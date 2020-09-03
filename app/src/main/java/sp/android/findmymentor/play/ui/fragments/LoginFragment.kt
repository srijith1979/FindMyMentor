package sp.android.findmymentor.play.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import kotlinx.android.synthetic.main.fragment_login.*
import sp.android.findmymentor.R
import sp.android.findmymentor.play.MainActivity
import sp.android.findmymentor.play.ui.viewmodels.MainViewModel
import sp.android.findmymentor.play.util.Event


class LoginFragment : Fragment(R.layout.fragment_login) {
    lateinit var viewModel: MainViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        addObservers()

        menteeRegisterId.setOnClickListener {
            registerMentee()
        }

        mentorRegisterID.setOnClickListener {
            registerMentor()
        }

        sign_in_button_id.setOnClickListener {
            viewModel.login(input_email.text.toString(), input_password.text.toString())
        }
    }

    private fun addObservers() {

        val loginObserver = Observer<Event<Task<AuthResult>>> { event ->
            event.getContentIfNotHandled()?.let {
                if (it.isSuccessful) {
                    viewModel.routeToUsersTimeline()
                } else {
                    Toast.makeText(requireContext(), "Login failed!!", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loginResult.observe(viewLifecycleOwner, loginObserver)

        val loggedInUserObserver = Observer<Event<Boolean>> { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    val bundle = Bundle().apply {
                        putSerializable("mentorArg", viewModel.loggedInMentor)
                    }
                    findNavController().navigate(
                            R.id.action_loginFragment_to_mentorHomeFragment,
                            bundle
                    )
                } else {
                    val bundle = Bundle().apply {
                        putSerializable("menteeArg", viewModel.loggedInMentee)
                    }
                    findNavController().navigate(
                            R.id.action_loginFragment_to_menteeHomeFragment,
                            bundle
                    )
                }
            }
        }

        viewModel.loggedInUserIsMentor.observe(viewLifecycleOwner, loggedInUserObserver)
    }


    private fun registerMentor() {
        val bundle = Bundle().apply {
            putBoolean("isMentor", true)
        }
        findNavController().navigate(
                R.id.action_loginFragment_to_userProfileFormFragment,
                bundle
        )
    }

    private fun registerMentee() {
        val bundle = Bundle().apply {
            putBoolean("isMentor", false)
        }
        findNavController().navigate(
                R.id.action_loginFragment_to_userProfileFormFragment,
                bundle
        )
    }
}