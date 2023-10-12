package com.digitaldream.toyibatskool.adapters

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldream.toyibatskool.R
import com.digitaldream.toyibatskool.models.MultiChoiceQuestion
import com.digitaldream.toyibatskool.models.MultipleChoiceOption
import com.digitaldream.toyibatskool.models.QuestionItem
import com.digitaldream.toyibatskool.models.SectionModel
import com.digitaldream.toyibatskool.models.ShortAnswerModel
import com.digitaldream.toyibatskool.utils.FunctionUtils
import com.squareup.picasso.Picasso

class AdminELearningQuizAdapter(
    private var itemList: MutableList<SectionModel>,
    private var userResponses: MutableMap<String, String>,
    private var userResponse: UserResponse
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val VIEW_TYPE_SECTION = 0
        private const val VIEW_TYPE_MULTI_CHOICE_OPTION = 1
        private const val VIEW_TYPE_SHORT_ANSWER = 2
    }

    private var picasso = Picasso.get()
    private lateinit var optionsAdapter: OptionAdapter
    private var currentQuestionCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SECTION -> {
                val view = inflater.inflate(R.layout.item_section_layout, parent, false)

                SectionViewHolder(view)
            }

            VIEW_TYPE_MULTI_CHOICE_OPTION -> {
                val view = inflater.inflate(
                    R.layout.item_multi_choice_option_layout, parent,
                    false
                )
                MultipleChoiceViewHolder(view)
            }

            VIEW_TYPE_SHORT_ANSWER -> {
                val view = inflater.inflate(
                    R.layout.item_short_answer_layout, parent,
                    false
                )
                ShortAnswerViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type: $viewType")

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemModel = itemList[position]
        when (holder) {
            is SectionViewHolder -> holder.bind(itemModel)

            is MultipleChoiceViewHolder -> {
                val question =
                    (itemModel.questionItem as? QuestionItem.MultiChoice)?.question ?: return
                currentQuestionCount = calculateQuestionCount(position)
                holder.bind(question)

            }

            is ShortAnswerViewHolder -> {
                val question =
                    (itemModel.questionItem as? QuestionItem.ShortAnswer)?.question ?: return
                currentQuestionCount = calculateQuestionCount(position)
                holder.bind(question)

            }
        }
    }

    override fun getItemCount() = itemList.size

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position].viewType) {
            "section" -> VIEW_TYPE_SECTION
            "multiple_choice" -> VIEW_TYPE_MULTI_CHOICE_OPTION
            "short_answer" -> VIEW_TYPE_SHORT_ANSWER
            else -> position
        }
    }


    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sectionTitle: TextView = itemView.findViewById(R.id.sectionTxt)

        fun bind(sectionModel: SectionModel) {
            sectionTitle.text = sectionModel.sectionTitle
        }
    }

    inner class MultipleChoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionCountTxt: TextView = itemView.findViewById(R.id.questionCountTxt)
        private val questionTxt: TextView = itemView.findViewById(R.id.questionTxt)
        private val questionImage: ImageView = itemView.findViewById(R.id.questionImage)
        private val optionRecyclerView: RecyclerView =
            itemView.findViewById(R.id.optionsRecyclerView)

        private var options: MutableList<MultipleChoiceOption>? = null

        fun bind(multiChoiceQuestion: MultiChoiceQuestion) {
            questionTxt.text = multiChoiceQuestion.questionText
            loadImage(itemView.context, multiChoiceQuestion.attachmentUri, questionImage)
            questionCountTxt.text = (currentQuestionCount).toString()

            options = multiChoiceQuestion.options

            val questionId = multiChoiceQuestion.questionId
            val selectedOption = userResponses[questionId]

            if (selectedOption != null) {
                setSelectedOption(selectedOption)
            }

            optionsAdapter = OptionAdapter(multiChoiceQuestion)

            setUpOptionsRecyclerView(optionRecyclerView)

        }

        private fun setSelectedOption(selectedOption: String) {
            val position = options?.indexOfFirst {
                it.optionText == selectedOption || it.attachmentName == selectedOption
            }
            options?.forEach { it.isSelected = false }
            position?.let { options?.get(it)?.isSelected = true }
        }

    }

    inner class ShortAnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionCountTxt: TextView = itemView.findViewById(R.id.questionCountTxt)
        private val questionTxt: TextView = itemView.findViewById(R.id.questionTxt)
        private val questionImage: ImageView = itemView.findViewById(R.id.questionImage)
        private val answerEditText: EditText = itemView.findViewById(R.id.answerEditText)

        fun bind(shortAnswer: ShortAnswerModel) {
            questionTxt.text = shortAnswer.questionText
            loadImage(itemView.context, shortAnswer.attachmentUri, questionImage)
            questionCountTxt.text = (currentQuestionCount).toString()

            val questionId = shortAnswer.questionId
            val typedAnswer = userResponses[questionId]

            if (typedAnswer != null) {
                setTypedAnswer(typedAnswer)
            }

            answerEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    userResponse.setTypedAnswer(
                        questionId,
                        s.toString().replace("\\s+".toRegex(), " ").trim()
                    )
                }

                override fun afterTextChanged(s: Editable?) {

                }
            })
        }

        private fun setTypedAnswer(answer: String) {
            answerEditText.setText(answer)
        }

    }


    inner class OptionAdapter(
        private val multiChoiceQuestion: MultiChoiceQuestion
    ) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

        private var isAnimationPlayed = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.item_options_layout, parent, false
            )

            return OptionViewHolder(view)
        }

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
            val options = multiChoiceQuestion.options?.get(position)

            holder.bind(options!!)
        }

        override fun getItemCount() = multiChoiceQuestion.options?.size!!

        inner class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val optionLayout: LinearLayout = itemView.findViewById(R.id.optionLayout)
            private val optionCard: CardView = itemView.findViewById(R.id.optionCard)
            private val optionLabel: TextView = itemView.findViewById(R.id.optionsLabel)
            private val optionTxt: TextView = itemView.findViewById(R.id.optionsTxt)
            private val optionImage: ImageView = itemView.findViewById(R.id.optionImage)

            private val labelList =
                arrayOf(
                    'A', 'B', 'C', 'D',
                    'E', 'F', 'G', 'H',
                    'I', 'J', 'K', 'L',
                    'M', 'N', '0', 'P'
                )

            fun bind(multipleChoiceOption: MultipleChoiceOption) {
                optionLabel.text = labelList[adapterPosition].toString()

                itemView.isSelected = multipleChoiceOption.isSelected

                if (itemView.isSelected) {
                    optionCard.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.context, R.color.test_color_2)
                    )

                    optionLayout.background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.circle6)

                    optionLabel.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.test_color_2
                        )
                    )
                } else {
                    optionLayout.background = ContextCompat.getDrawable(
                        itemView.context, R
                            .drawable.circle5
                    )

                    optionCard.setCardBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context, R.color.color_1
                        )
                    )

                    optionLabel.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.color_1
                        )
                    )
                }

                if (multipleChoiceOption.optionText.isEmpty()) {
                    loadImage(itemView.context, multipleChoiceOption.attachmentUri, optionImage)
                    optionTxt.isVisible = false
                } else {
                    optionTxt.text = multipleChoiceOption.optionText
                    optionTxt.isVisible = true
                    optionImage.isVisible = false
                }

                if (!isAnimationPlayed) {
                    playAnimation(itemView, adapterPosition)
                }

                itemView.setOnClickListener {
                    val selectedOption = multiChoiceQuestion.options?.get(adapterPosition)
                    val questionId = multiChoiceQuestion.questionId

                    multiChoiceQuestion.options?.forEach { it.isSelected = false }

                    selectedOption!!.isSelected = true
                    isAnimationPlayed = true

                    notifyDataSetChanged()

                    userResponse.onOptionSelected(questionId,
                        selectedOption.optionText.ifEmpty {
                            selectedOption.attachmentName
                        }
                    )

                }
            }
        }
    }

    private fun playAnimation(itemView: View, position: Int) {
        itemView.alpha = 0f
        itemView.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(position * 150L)
            .start()
    }

    private fun setUpOptionsRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            hasFixedSize()
            layoutManager = LinearLayoutManager(context)
            adapter = optionsAdapter
        }
    }


    private fun loadImage(context: Context, imageUri: Any?, imageView: ImageView) {
        try {
            when (imageUri) {
                is String -> {
                    if (imageUri.isNotEmpty()) {
                        val isBase64 = FunctionUtils.isBased64(imageUri)

                        if (isBase64) {
                            val bitmap = FunctionUtils.decodeBase64ToBitmap(imageUri)
                            imageView.isVisible = bitmap != null
                            imageView.setImageBitmap(bitmap)
                        } else {
                            val url = "${context.getString(R.string.base_url)}/$imageUri"
                            picasso.load(url).into(imageView)
                            imageView.isVisible = true
                        }

                    } else {
                        imageView.isVisible = false
                    }
                }

                is Uri -> {
                    picasso.load(imageUri).into(imageView)
                    imageView.isVisible = true
                }

                else -> imageView.isVisible = false

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateQuestionCount(currentPosition: Int): Int {
        var count = 0
        for (i in 0 until currentPosition) {
            if (itemList[i].viewType != "section") {
                count++
            }
        }
        return count + 1
    }


    interface UserResponse {
        fun onOptionSelected(questionId: String, selectedOption: String)
        fun setTypedAnswer(questionId: String, typedAnswer: String)
    }
}