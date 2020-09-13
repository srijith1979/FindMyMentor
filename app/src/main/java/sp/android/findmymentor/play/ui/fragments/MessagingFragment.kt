package sp.android.findmymentor.play.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_messaging.*
import sp.android.findmymentor.R
import sp.android.findmymentor.play.MainActivity
import sp.android.findmymentor.play.adapters.MessagingAdapter
import sp.android.findmymentor.play.firebase.FirebaseSource
import sp.android.findmymentor.play.models.Message
import sp.android.findmymentor.play.repository.MainRepository
import sp.android.findmymentor.play.ui.viewmodels.LoginViewModel
import sp.android.findmymentor.play.ui.viewmodels.MessagingViewModel
import sp.android.findmymentor.play.ui.viewmodels.factories.MessagingViewModelFactory
import sp.android.findmymentor.play.util.Constants.Companion.loggedInUserName

class MessagingFragment : Fragment(R.layout.fragment_messaging) {
    val args: MessagingFragmentArgs by navArgs()
    lateinit var viewModel: MessagingViewModel
    lateinit var loginViewModel: LoginViewModel
    lateinit var messagingAdapter: MessagingAdapter
    var message: Message = Message()
    lateinit var observer: Observer<MutableList<Message>>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        message = args.messageArg
        initViewModel()
        setUpRecylcerView()
        setListeners()
        addObservers()
    }

    private fun initViewModel() {
        loginViewModel = (activity as MainActivity).viewModel
        val mainRepository = MainRepository(FirebaseSource())
        viewModel = ViewModelProvider(this, MessagingViewModelFactory(mainRepository, message.chatKeyValue.toString())).get(MessagingViewModel::class.java)
    }

    private fun setUpRecylcerView() {
        messagingAdapter = MessagingAdapter()
        loggedInUserName = loginViewModel.getLoggedInUserName()!!
        messagesRecyclerView.adapter = messagingAdapter

        val layoutManager = LinearLayoutManager(activity)
        messagesRecyclerView.layoutManager = layoutManager

        messagesRecyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            messagesRecyclerView.scrollToPosition(messagingAdapter.currentList.size - 1)
        }
    }

    private fun setListeners() {
        inputMessage.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.toString().trim().isNotEmpty()) {
                    sendMessageImageView.setEnabled(true)
                } else {
                    sendMessageImageView.setEnabled(false)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        })

        sendMessageImageView.setOnClickListener {
            val message = Message(loginViewModel.getLoggedInEmailAddress()!!, loginViewModel.getLoggedInUserName()!!, inputMessage.text.toString(), System.currentTimeMillis())
            this.message.chatKeyValue?.let { chatKeyValue -> viewModel.sendMessage(message, chatKeyValue) }
            inputMessage.setText("")
        }
    }

    private fun addObservers() {
        observer = Observer {
            messagingAdapter.submitList(it)
            messagesRecyclerView.scrollToPosition(messagingAdapter.currentList.size - 1)
        }

        viewModel.messagesLiveData.observe(viewLifecycleOwner, observer)
    }
}