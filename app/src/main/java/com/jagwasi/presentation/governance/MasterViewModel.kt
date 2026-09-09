 * **Unused/Unresolved Log import:** android.util.Log is imported on line 3 but never used.
 * **Over-allocation via java.util.Random:** Standard Kotlin best practice for generating random elements from lists is Random.nextInt() or wordList.random(), avoiding unnecessary instantiation of java.util.Random().
