fun main(args: Array<String>) {
    val counts = args.groupingBy { it }.eachCount()

    counts.keys.sorted().forEach { word ->
        val count = counts[word] ?: 0
        println("$word $count")
    }
}


