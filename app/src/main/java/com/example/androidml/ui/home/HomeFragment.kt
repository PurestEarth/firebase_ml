package com.example.androidml.ui.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.androidml.R
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var contentIV: ImageView
    private lateinit var tagsTV: TextView
    private val pickPhotoRequestCode = 123

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        // val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {

        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: Button = view.findViewById(R.id.button)
        tagsTV = view.findViewById(R.id.textView)
        contentIV = view.findViewById(R.id.imageView)
        button.setOnClickListener { view ->
            pickImage()
        }
    }
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, pickPhotoRequestCode)
    }

    override fun onActivityResult(requestCode: Int,
                                  resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                pickPhotoRequestCode -> {
                    val bitmap = getImageFromData(data)
                    bitmap?.apply {
                        contentIV.setImageBitmap(this)
                        processImageTagging(bitmap)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode,
            data)
    }

    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(
            getActivity()?.getContentResolver(),
            selectedImage)
    }

    private fun processImageTagging(bitmap: Bitmap) {
        val visionImg =
            FirebaseVisionImage.fromBitmap(bitmap)
        val labeler =
            FirebaseVision.getInstance().getOnDeviceImageLabeler().
                processImage(visionImg)
                .addOnSuccessListener { tags ->
                    tagsTV.text = tags.joinToString(" ") {
                        it.text
                        }
                }
                .addOnFailureListener { ex ->
                    Log.wtf("LAB", ex)
                }
    }
}
