package com.digitaldream.toyibatskool.fragments

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldream.toyibatskool.R
import com.digitaldream.toyibatskool.activities.AdminELearningActivity
import com.digitaldream.toyibatskool.adapters.AdminELearningCommentAdapter
import com.digitaldream.toyibatskool.adapters.AdminELearningFilesAdapter
import com.digitaldream.toyibatskool.models.AttachmentModel
import com.digitaldream.toyibatskool.models.CommentDataModel
import com.digitaldream.toyibatskool.utils.FileViewModel
import com.digitaldream.toyibatskool.utils.FunctionUtils.formatDate2
import com.digitaldream.toyibatskool.utils.FunctionUtils.getDate
import org.json.JSONArray
import org.json.JSONObject


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AdminELearningMaterialDetailsFragment :
    Fragment(R.layout.fragment_admin_e_learning_material_details) {


    private lateinit var titleTxt: TextView
    private lateinit var descriptionTxt: TextView
    private lateinit var attachmentTxt: TextView
    private lateinit var attachmentRecyclerView: RecyclerView
    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var commentGuide: View
    private lateinit var commentTitleTxt: TextView
    private lateinit var commentEditText: EditText
    private lateinit var sendMessageBtn: ImageButton

    private lateinit var commentAdapter: AdminELearningCommentAdapter
    private lateinit var filesAdapter: AdminELearningFilesAdapter
    private val commentList = mutableListOf<CommentDataModel>()
    private var fileList = mutableListOf<AttachmentModel>()

    private lateinit var fileViewModel: FileViewModel
    private lateinit var menuHost: MenuHost

    private var jsonData: String? = null
    private var taskType: String? = null
    private var title: String? = null
    private var description: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jsonData = it.getString(ARG_PARAM1)
            taskType = it.getString(ARG_PARAM2)
        }

        fileViewModel = ViewModelProvider(this)[FileViewModel::class.java]

    }


    companion object {

        @JvmStatic
        fun newInstance(data: String, task: String) =
            AdminELearningMaterialDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, data)
                    putString(ARG_PARAM2, task)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpViews(view)

        setUpMenu()

        onWatchEditText()

        setUpCommentRecyclerView()

        parseFileJsonObject()

        updateComment()
    }

    private fun setUpViews(view: View) {
        view.apply {
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            titleTxt = findViewById(R.id.titleTxt)
            descriptionTxt = findViewById(R.id.descriptionTxt)
            attachmentTxt = findViewById(R.id.attachmentTxt)
            attachmentRecyclerView = findViewById(R.id.attachmentRecyclerView)
            commentRecyclerView = findViewById(R.id.commentRecyclerView)
            commentGuide = findViewById(R.id.commentGuide)
            commentTitleTxt = findViewById(R.id.commentTitleTxt)
            commentEditText = findViewById(R.id.commentEditText)
            sendMessageBtn = findViewById(R.id.sendMessageBtn)

            menuHost = requireActivity()
            (requireContext() as AppCompatActivity).setSupportActionBar(toolbar)
            val actionBar = (requireContext() as AppCompatActivity).supportActionBar

            actionBar?.apply {
                title = ""
                setDisplayHomeAsUpEnabled(true)
                setHomeButtonEnabled(true)
            }
        }
    }

    private fun parseFileJsonObject() {
        try {
            if (jsonData?.isNotBlank() == true) {
                JSONObject(jsonData!!).let {
                    title = it.getString("title")
                    description = it.getString("description")
                    parseFilesArray(JSONArray(it.getString("picref")))
                }

                setTextOnViews()

                setUpFilesRecyclerView()

                attachmentTxt.isVisible = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setTextOnViews() {
        titleTxt.text = title
        descriptionTxt.text = description
    }

    private fun parseFilesArray(files: JSONArray) {
        for (i in 0 until files.length()) {
            files.getJSONObject(i).let {
                val fileName = trimText(it.getString("file_name"))
                val oldFileName = trimText(it.getString("file_name"))
                val type = it.getString("type")
                val uri = it.getString("file_name")

                val attachmentModel = AttachmentModel(fileName, oldFileName, type, uri)

                fileList.add(attachmentModel)
            }
        }
    }

    private fun trimText(text: String): String {
        return text.replace("../assets/elearning/practice/", "").ifEmpty { "" }
    }

    private fun setUpCommentRecyclerView() {
        commentAdapter = AdminELearningCommentAdapter(commentList)

        commentTitleTxt.isVisible = commentList.isNotEmpty()
        commentGuide.isVisible = commentList.isNotEmpty()

        commentRecyclerView.apply {
            hasFixedSize()
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentAdapter
        }

    }

    private fun setUpFilesRecyclerView() {
        filesAdapter = AdminELearningFilesAdapter(parentFragmentManager, fileList, fileViewModel)

        attachmentRecyclerView.apply {
            hasFixedSize()
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = filesAdapter
        }

    }

    private fun onWatchEditText() {
        sendMessageBtn.isEnabled = false

        commentEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                sendMessageBtn.isEnabled = s.toString().isNotBlank()

                if (sendMessageBtn.isEnabled) {
                    sendMessageBtn.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(), R.color.black
                        ),
                        PorterDuff.Mode.SRC_IN
                    )
                } else {
                    sendMessageBtn.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(), R.color.test_color_7
                        ),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }
        })
    }

    private fun sendComment() {
        val message = commentEditText.text.toString().trim()
        val date = formatDate2(getDate())

        val commentDataModel = CommentDataModel(
            "", "","",
             "", message, date
        )

        commentList.add(commentDataModel)

        commentTitleTxt.isVisible = commentList.isNotEmpty()
        commentGuide.isVisible = commentList.isNotEmpty()

        hideKeyboard(commentEditText)

        commentAdapter.notifyDataSetChanged()

    }


    private fun updateComment() {
        sendMessageBtn.setOnClickListener {
            sendComment()
        }

    }


    private fun hideKeyboard(editText: EditText) {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        editText.clearFocus()
        editText.setText("")
    }

    private fun setUpMenu() {
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_e_learning_details, menu)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.refresh -> {

                        true
                    }

                    R.id.edit -> {
                        startActivity(
                            Intent(requireContext(), AdminELearningActivity::class.java)
                                .putExtra("from", "material")
                                .putExtra("task", "edit")
                                .putExtra("json", jsonData)
                        )

                        true
                    }

                    R.id.delete -> {
                        true
                    }

                    else -> {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                        false
                    }

                }
            }
        })
    }

}