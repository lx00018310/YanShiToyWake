"""固定内容库（实施计划书 §14）。

AI 失败或 mock 模式时使用。按玩具类型选择，会话内通过 spark_count 取模避免立即重复。
每条固定内容同时返回家长提示（§14 选择规则：固定内容也必须返回家长提示）。
"""

from dataclasses import dataclass


@dataclass(frozen=True)
class FixedSpark:
    child_speech: str
    parent_hint: str


_PLUSH = [
    FixedSpark("{name}有点冷，给它找块小被子吧。", "等孩子去找被子，不要替他拿。"),
    FixedSpark("{name}肚子饿了，你想给它吃什么？", "让孩子决定食物，不要纠正。"),
    FixedSpark("{name}找不到家了，你带它去哪里？", "问孩子哪里可以当家，不要替他选。"),
    FixedSpark("{name}想坐小车，你放它上去好吗？", "等孩子行动，不要替他放。"),
    FixedSpark("{name}困了，我们给它找个睡觉的地方。", "让孩子选地方，不要指定。"),
]

_VEHICLE = [
    FixedSpark("{name}要运两块积木。", "让孩子去找积木，不要替他拿。"),
    FixedSpark("{name}前面的路被挡住了。", "问孩子怎么过去，不要替他决定。"),
    FixedSpark("{name}听见桌子下面有人叫它。", "等孩子去查看，不要催促。"),
    FixedSpark("{name}要过一座桥，我们搭一座吧。", "和孩子一起搭，不要替他搭完。"),
    FixedSpark("{name}累了，给它找个停车的地方。", "让孩子选位置，不要指定。"),
]

_FIGURE = [
    FixedSpark("{name}今天没有家，我们搭一个吧。", "让孩子决定怎么搭，不要替他搭完。"),
    FixedSpark("{name}想去找一位朋友。", "问孩子找谁，不要替他选。"),
    FixedSpark("{name}藏起来了，我们找找看。", "和孩子一起找，不要马上指出。"),
    FixedSpark("{name}要把这个东西送过去。", "让孩子决定送到哪里，不要指定。"),
    FixedSpark("{name}想邀请一个玩具来做客。", "让孩子选玩具，不要替他选。"),
]

_GENERIC = [
    FixedSpark("{name}想找一个小伙伴一起玩。", "让孩子选伙伴，不要替他选。"),
    FixedSpark("{name}今天要去一个新地方。", "问孩子去哪里，不要指定。"),
    FixedSpark("{name}好像在找什么东西。", "等孩子行动，不要催促。"),
    FixedSpark("{name}想排队等一等。", "让孩子排，不要替他排。"),
    FixedSpark("{name}准备好了，我们要出发啦。", "问孩子去哪里，不要替他决定。"),
]

# 玩具类型 -> 固定内容
_LIBRARY: dict[str, list[FixedSpark]] = {
    "毛绒动物": _PLUSH,
    "玩具车": _VEHICLE,
    "人偶": _FIGURE,
    "积木角色": _FIGURE,
    "过家家物品": _GENERIC,
    "其他": _GENERIC,
}


def pick_spark(toy_type: str, name: str, spark_count: int) -> FixedSpark:
    """按 spark_count 取模选择，避免会话内立即重复。"""
    sparks = _LIBRARY.get(toy_type, _GENERIC)
    idx = spark_count % len(sparks)
    chosen = sparks[idx]
    return FixedSpark(
        child_speech=chosen.child_speech.replace("{name}", name),
        parent_hint=chosen.parent_hint,
    )
