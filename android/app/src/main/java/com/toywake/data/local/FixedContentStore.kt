package com.toywake.data.local

/**
 * 本地固定内容库（镜像后端 §14），用于后端不可达时的离线降级（实施计划书 §16.1）。
 *
 * 保证无网络时仍可生成游戏火花，App 不中断、不崩溃。
 */
object FixedContentStore {

    data class FixedSpark(val childSpeech: String, val parentHint: String)

    private val PLUSH = listOf(
        FixedSpark("{name}有点冷，给它找块小被子吧。", "等孩子去找被子，不要替他拿。"),
        FixedSpark("{name}肚子饿了，你想给它吃什么？", "让孩子决定食物，不要纠正。"),
        FixedSpark("{name}找不到家了，你带它去哪里？", "问孩子哪里可以当家，不要替他选。"),
        FixedSpark("{name}想坐小车，你放它上去好吗？", "等孩子行动，不要替他放。"),
        FixedSpark("{name}困了，我们给它找个睡觉的地方。", "让孩子选地方，不要指定。"),
    )
    private val VEHICLE = listOf(
        FixedSpark("{name}要运两块积木。", "让孩子去找积木，不要替他拿。"),
        FixedSpark("{name}前面的路被挡住了。", "问孩子怎么过去，不要替他决定。"),
        FixedSpark("{name}听见桌子下面有人叫它。", "等孩子去查看，不要催促。"),
        FixedSpark("{name}要过一座桥，我们搭一座吧。", "和孩子一起搭，不要替他搭完。"),
        FixedSpark("{name}累了，给它找个停车的地方。", "让孩子选位置，不要指定。"),
    )
    private val FIGURE = listOf(
        FixedSpark("{name}今天没有家，我们搭一个吧。", "让孩子决定怎么搭，不要替他搭完。"),
        FixedSpark("{name}想去找一位朋友。", "问孩子找谁，不要替他选。"),
        FixedSpark("{name}藏起来了，我们找找看。", "和孩子一起找，不要马上指出。"),
        FixedSpark("{name}要把这个东西送过去。", "让孩子决定送到哪里，不要指定。"),
        FixedSpark("{name}想邀请一个玩具来做客。", "让孩子选玩具，不要替他选。"),
    )
    private val GENERIC = listOf(
        FixedSpark("{name}想找一个小伙伴一起玩。", "让孩子选伙伴，不要替他选。"),
        FixedSpark("{name}今天要去一个新地方。", "问孩子去哪里，不要指定。"),
        FixedSpark("{name}好像在找什么东西。", "等孩子行动，不要催促。"),
        FixedSpark("{name}想排队等一等。", "让孩子排，不要替他排。"),
        FixedSpark("{name}准备好了，我们要出发啦。", "问孩子去哪里，不要替他决定。"),
    )

    private val LIBRARY: Map<String, List<FixedSpark>> = mapOf(
        "毛绒动物" to PLUSH,
        "玩具车" to VEHICLE,
        "人偶" to FIGURE,
        "积木角色" to FIGURE,
        "过家家物品" to GENERIC,
        "其他" to GENERIC,
    )

    fun pickSpark(toyType: String, name: String, index: Int): FixedSpark {
        val list = LIBRARY[toyType] ?: GENERIC
        val spark = list[index % list.size]
        return FixedSpark(
            childSpeech = spark.childSpeech.replace("{name}", name),
            parentHint = spark.parentHint,
        )
    }

    fun wakePhrase(name: String): String = "${name}醒啦。"

    fun endingSpeech(name: String): String = "${name}累了，我们送它回家吧。"
}
