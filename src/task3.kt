fun main(args: Array<String>) {
    args.toSet()
        .sorted()
        .forEach { word ->
            println(word)
        }
}


