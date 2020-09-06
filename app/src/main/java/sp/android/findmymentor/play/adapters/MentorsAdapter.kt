package sp.android.findmymentor.play.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ramotion.foldingcell.FoldingCell
import kotlinx.android.synthetic.main.cell_folded_layout.view.*
import kotlinx.android.synthetic.main.cell_unfolded_layout.view.*
import sp.android.findmymentor.R
import sp.android.findmymentor.play.models.Mentor
import sp.android.findmymentor.play.util.Constants
import java.util.HashSet

/*
Adapter class for the recycler view
* */
class MentorsAdapter : RecyclerView.Adapter<MentorsAdapter.MentorViewHolder>() {
    private var unfoldedIndexes = HashSet<Int>()
    private var onItemClickListener: ((View, Int) -> Unit)? = null
    private var onRequestMentorClickListener: ((Mentor) -> Unit)? = null
    private var onCommonGroupsClickListener: ((Mentor) -> Unit)? = null
    private var chatKeys = mutableSetOf<String>()
    private var loggedInUserEmail: String = ""

    inner class MentorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Mentor>() {
        override fun areItemsTheSame(oldItem: Mentor, newItem: Mentor): Boolean {
            return oldItem.email_address == newItem.email_address
        }

        override fun areContentsTheSame(oldItem: Mentor, newItem: Mentor): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): MentorViewHolder {
        return MentorViewHolder(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.cell,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MentorsAdapter.MentorViewHolder, position: Int) {
        val mentor = differ.currentList[position]

        holder.itemView.apply {
            val cell = this as FoldingCell

            //folded scenario
            roundedTextViewFolded.setText(mentor.full_name.subSequence(0, 1))
            availabilityTextViewFolded.setText("${mentor.availability}/${mentor.totalSpots}")
            userNameTextViewFolded.setText(mentor.full_name)
            userRoleTextViewFolded.setText(mentor.role)
            organizationNameTextViewFolded.setText(mentor.organization)
            locationTextViewFolded.setText(mentor.location)

            //unfolded scenario
            roundedTextViewUnfolded.setText(mentor.full_name.subSequence(0, 1))
            userNameTextViewUnfolded.setText(mentor.full_name)
            userRoleTextViewUnfolded.setText(mentor.role)
            aboutUserLabelTextView.setText(String.format(context.getString(R.string.description), mentor.full_name.substringBefore(' ')))
            aboutUserDescriptionTextView.setText(mentor.aboutYourself)
            availabilityTextViewUnfolded.setText(mentor.availability.toString())
            organizationNameTextViewUnfolded.setText(mentor.organization)
            availableSpotsTextView.setText(mentor.availability.toString())
            locationTextViewUnfolded.setText(mentor.location)
            availabilityStatusTVUnfolded.setText("Available")//todo

            requestUserButton.setOnClickListener {
                requestUser(requestUserButton)
                onRequestMentorClickListener?.let {
                    it(mentor)
                }
            }

            val chatKey = Constants.getKey(loggedInUserEmail, mentor.email_address)

            if (chatKeys.contains(chatKey)) {
                requestUser(requestUserButton)
            }

            commonInterestsImageView.setOnClickListener {

                onCommonGroupsClickListener?.let {
                    it(mentor)
                }
            }
            // for existing cell set valid valid state(without animation todo revisit this logic
            /*if (unfoldedIndexes.contains(position)) {
                cell.unfold(true)
            } else {
                cell.fold(true)
            }*/

            setOnClickListener {
                onItemClickListener?.let {
                    it(this, position)
                }
            }
        }
    }

    fun requestUser(requestUserButton : TextView){
        requestUserButton.setText("Requested")
        requestUserButton.alpha=0.5f
        requestUserButton.isEnabled= false
    }

    // simple methods for register cell state changes
    fun registerToggle(position: Int) {
        if (unfoldedIndexes.contains(position)) registerFold(position) else registerUnfold(position)
    }

    fun registerFold(position: Int) {
        unfoldedIndexes.remove(position)
    }

    fun registerUnfold(position: Int) {
        unfoldedIndexes.add(position)
    }

    fun setKeysAndLoggedInUserEmail(keys: MutableSet<String>, email: String) {
        chatKeys = keys
        loggedInUserEmail = email
    }

    /*
    on click listener for the recycler view row item
    * */
    fun setOnItemClickListener(listener: (View, Int) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnRequestMentorClickListener(listener: (Mentor) -> Unit) {
        onRequestMentorClickListener = listener
    }

    fun setCommonGroupsClickListener(listener: (Mentor) -> Unit) {
        onCommonGroupsClickListener = listener
    }

    /*
    submits the hacker news response to the differ util, also saves
    the response to fullList (used by search)
     */
    fun submitList(stores: List<Mentor>) {
        differ.submitList(stores)
        notifyDataSetChanged()
    }
}