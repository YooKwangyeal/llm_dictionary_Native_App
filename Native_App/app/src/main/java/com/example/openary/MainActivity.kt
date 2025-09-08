package com.example.openary


import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var englishBtn: Button
    private lateinit var koreanBtn: Button
    private lateinit var posFilter: Spinner
    private lateinit var lengthFilter: Spinner
    private lateinit var rarityFilter: Spinner
    private lateinit var totalWords: TextView
    private lateinit var searchResults: TextView
    private lateinit var avgScore: TextView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var resultsRecyclerView: RecyclerView

    private var dictionaryData: List<DictionaryWord> = listOf()
    private var currentResults: List<DictionaryWord> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchInput = findViewById(R.id.searchInput)
        englishBtn = findViewById(R.id.englishBtn)
        koreanBtn = findViewById(R.id.koreanBtn)
        posFilter = findViewById(R.id.posFilter)
        lengthFilter = findViewById(R.id.lengthFilter)
        rarityFilter = findViewById(R.id.rarityFilter)
        totalWords = findViewById(R.id.totalWords)
        searchResults = findViewById(R.id.searchResults)
        avgScore = findViewById(R.id.avgScore)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView)

        // Spinner 어댑터 설정
        posFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("전체", "noun", "verb", "adjective", "adverb", "other"))
        lengthFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("전체", "3-4글자", "5-6글자", "7-8글자", "9글자 이상"))
        rarityFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("전체", "1", "2", "3", "4+"))

        // RecyclerView 어댑터 설정
        val adapter = DictionaryAdapter(listOf())
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = adapter

        // JSON 데이터 로드 (assets에서)
        dictionaryData = loadDictionaryFromAssets("llm_dictionary_2letter_20250812_000751.json")
        updateStats()

        // 이벤트 연결
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                search(adapter)
            }
        })

        englishBtn.setOnClickListener {
            // Language button functionality can be added here
            searchInput.hint = "검색할 단어를 입력하세요..."
        }

        koreanBtn.setOnClickListener {
            // Language button functionality can be added here
            searchInput.hint = "Search for words..."
        }
        posFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                search(adapter)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        lengthFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                search(adapter)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        rarityFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                search(adapter)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadDictionaryFromAssets(fileName: String): List<DictionaryWord> {
        return try {
            val json = assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonObj = org.json.JSONObject(json)
            val wordsArray = when {
                jsonObj.has("entries") -> jsonObj.getJSONArray("entries")
                jsonObj.has("words") -> jsonObj.getJSONArray("words")
                else -> org.json.JSONArray(json)
            }
            (0 until wordsArray.length()).map { i ->
                val obj = wordsArray.getJSONObject(i)
                DictionaryWord(
                    word = obj.getString("word"),
                    pos = obj.optString("pos", null),
                    length = obj.optInt("length", obj.getString("word").length),
                    rarity = obj.optInt("rarity", 3),
                    score = obj.optDouble("score", 0.0),
                    confidence = obj.optDouble("confidence", 0.0),
                    definition = obj.optString("definition", null),
                    example = obj.optString("example", null)
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "사전 파일 로드 실패: ${e.message}", Toast.LENGTH_LONG).show()
            listOf()
        }
    }

    private fun search(adapter: DictionaryAdapter) {
        val query = searchInput.text.toString().trim().lowercase()
        val posValue = posFilter.selectedItem.toString()
        val lengthValue = lengthFilter.selectedItem.toString()
        val rarityValue = rarityFilter.selectedItem.toString()

        if (dictionaryData.isEmpty()) {
            Toast.makeText(this, "사전 데이터가 로드되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        if (query.isEmpty()) {
            adapter.updateData(listOf())
            searchResults.text = "검색 결과: 0"
            return
        }

        loadingSpinner.visibility = View.VISIBLE

        val results = dictionaryData.filter { word ->
            val matchesText = word.word.lowercase().contains(query) ||
                (word.definition?.lowercase()?.contains(query) == true) ||
                (word.example?.lowercase()?.contains(query) == true)
            val matchesPos = posValue == "전체" || word.pos == posValue
            val len = word.length ?: word.word.length
            val matchesLength = when (lengthValue) {
                "3-4글자" -> len in 3..4
                "5-6글자" -> len in 5..6
                "7-8글자" -> len in 7..8
                "9글자 이상" -> len >= 9
                else -> true
            }
            val matchesRarity = when (rarityValue) {
                "전체" -> true
                "4+" -> (word.rarity ?: 3) >= 4
                else -> (word.rarity ?: 3) == rarityValue.toIntOrNull()
            }
            matchesText && matchesPos && matchesLength && matchesRarity
        }.sortedWith(compareByDescending<DictionaryWord> {
            it.word.lowercase() == query
        }.thenByDescending {
            it.word.lowercase().startsWith(query)
        }.thenByDescending {
            it.score ?: 0.0
        })

        currentResults = results
        adapter.updateData(results.take(50))
        searchResults.text = "검색 결과: ${results.size}"
        updateStats()
        loadingSpinner.visibility = View.GONE
    }

    private fun updateStats() {
        totalWords.text = "총 단어 수: ${dictionaryData.size}"
        searchResults.text = "검색 결과: ${currentResults.size}"
        avgScore.text = if (dictionaryData.isNotEmpty())
            "평균 점수: %.2f".format(dictionaryData.map { it.score ?: 0.0 }.average())
        else "평균 점수: 0.00"
    }
}