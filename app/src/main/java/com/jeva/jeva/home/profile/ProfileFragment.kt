package com.jeva.jeva.home.profile

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jeva.jeva.Database
import com.jeva.jeva.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private val db = Database()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val profilePicView: ImageView = root.findViewById(R.id.fragmentProfilePhoto)
        val req = RequestOptions()
            .placeholder(ColorDrawable(Color.LTGRAY))
            .error(ColorDrawable(Color.RED)) // default profile pic

        db.getCurrentUser() { userData ->
            if (userData != null) {
                fragmentProfileUser?.text = userData["name"] as CharSequence
                fragmentProfileUsername?.text = userData["username"] as CharSequence
        }   else {
                Log.e("Profile Error: ","User information not found")
                // error toast || reload ?
            }
        }

        Glide.with(this)
            .applyDefaultRequestOptions(req)
            .load(db.getCurrentUserProfilePicRef())
            .into(profilePicView)

        return root
    }

}