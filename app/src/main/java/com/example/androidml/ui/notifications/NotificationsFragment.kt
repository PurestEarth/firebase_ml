package com.example.androidml.ui.notifications

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
import com.example.androidml.ui.dashboard.DashboardViewModel
import com.example.androidml.ui.home.HomeViewModel
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector
import com.google.firebase.ml.vision.text.FirebaseVisionText

class NotificationsFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var contentIV: ImageView
    private val pickPhotoRequestCode = 123

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        notificationsViewModel.text.observe(viewLifecycleOwner, Observer {

        })
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: Button = view.findViewById(R.id.button3)

        contentIV = view.findViewById(R.id.imageView2)
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
            FirebaseVision.getInstance().onDeviceObjectDetector.
                processImage(visionImg)
                .addOnSuccessListener {  detectedObjects ->
                    Log.i("OUT", detectedObjects.size.toString())
                    for (obj in detectedObjects) {

                        val id = obj.trackingId       // A number that identifies the object across images
                        val bounds = obj.boundingBox  // The object's position in the image
                        // If classification was enabled:
                        val category = obj.classificationCategory
                        val confidence = obj.classificationConfidence
                    }
                }
                .addOnFailureListener { ex ->
                    Log.wtf("LAB", ex)
                }
    }

}
