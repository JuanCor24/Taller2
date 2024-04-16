package com.example.taller2.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.taller2.databinding.ActivityCamaraBinding
import com.example.taller2.utils.Alerts
import com.example.taller2.BuildConfig
import java.io.File
import java.text.DateFormat.getDateInstance
import java.util.Date
import java.util.Objects

class CamaraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCamaraBinding
    private val TAG = CamaraActivity::class.java.simpleName
    private val alerts = Alerts(this)

    val PERM_CAMERA_CODE = 101
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_VIDEO_CAPTURE = 2
    var outputPath: Uri? = null

    val PERM_GALERY_GROUP_CODE = 202
    val REQUEST_PICK = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamaraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonCamera.setOnClickListener() {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    takePhotoOrVideo()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    alerts.indefiniteSnackbar(
                        binding.root,
                        "El permiso de Camara es necesario para usar esta actividad 😭"
                    )
                }

                else -> {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERM_CAMERA_CODE)
                }
            }
        }
        binding.buttonGalery.setOnClickListener(){
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    alerts.indefiniteSnackbar(
                        binding.root,
                        "El permiso de Galeria es necesario para usar esta actividad 😭"
                    )
                }

                else -> {
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.plus(Manifest.permission.READ_MEDIA_IMAGES)
                        permissions.plus(Manifest.permission.READ_MEDIA_VIDEO)
                    }
                    requestPermissions(permissions, PERM_GALERY_GROUP_CODE)
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_CAMERA_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoOrVideo()
                } else {
                    alerts.shortSimpleSnackbar(
                        binding.root,
                        "Me acaban de negar los permisos de Camara 😭"
                    )
                }
            }
                PERM_GALERY_GROUP_CODE -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startGallery()
                    } else {
                        alerts.shortSimpleSnackbar(
                            binding.root,
                            "Me acaban de negar los permisos de Galería 😭"
                        )
                    }
                }

            }
        }


    private fun takePhotoOrVideo() {
        if (binding.isPhotoOrVideoSwitch.isChecked)
            dispatchTakeVideoIntent()
        else
            dispatchTakePictureIntent()
    }

    private fun dispatchTakeVideoIntent() {
        Intent(MediaStore.ACTION_VIDEO_CAPTURE).also { takeVideoIntent ->
//            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
//            takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            val file = File(
                getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString() + File.separator + "${
                    getDateInstance().format(Date())
                }.mp4"
            )
            outputPath = FileProvider.getUriForFile( Objects.requireNonNull(applicationContext),
                BuildConfig.APPLICATION_ID + ".contentprovider", file)
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPath)
            takeVideoIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE)
            } ?: run {
                //display error state to the user
                alerts.shortSimpleSnackbar(binding.root, "No se pudo tomar el video")
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val imageFileName = "${getDateInstance().format(Date())}.jpg"
        val imageFile =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + imageFileName)
        outputPath = FileProvider.getUriForFile(
            Objects.requireNonNull(applicationContext),
             BuildConfig.APPLICATION_ID + ".contentprovider",
            imageFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPath)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            alerts.indefiniteSnackbar(binding.root, e.localizedMessage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    alerts.shortSimpleSnackbar(binding.root, "Foto tomada correctamente")
                    Log.d(TAG, "onActivityResult: ${outputPath}")
                    binding.preview.removeAllViews()
                    val imageView = ImageView(this)
                    imageView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    imageView.setImageURI(outputPath)
                    imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                    imageView.adjustViewBounds = true
                    binding.preview.addView(imageView)
                } else {
                    alerts.shortSimpleSnackbar(binding.root, "No se pudo tomar la foto")
                }
            }

            REQUEST_VIDEO_CAPTURE -> {
                if (resultCode == RESULT_OK) {
                    alerts.shortSimpleSnackbar(binding.root, "Video tomado correctamente")
                    binding.preview.removeAllViews()
                    val videoView: VideoView = VideoView(this)
                    videoView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    videoView.setVideoURI(outputPath)
                    videoView.foregroundGravity = View.TEXT_ALIGNMENT_CENTER
                    videoView.setMediaController(MediaController(this))
                    videoView.start()
                    videoView.setZOrderOnTop(true)
                    binding.preview.addView(videoView)
                } else {
                    alerts.shortSimpleSnackbar(binding.root, "No se pudo tomar el video")
                }
            }
            REQUEST_PICK -> {
                if (resultCode == RESULT_OK) {
                    alerts.shortSimpleSnackbar(
                        binding.root,
                        "Se selecciono un archivo de la galeria"
                    )
                    if (data != null) {
                        val uri = data.data
                        binding.preview.removeAllViews()
                        if (binding.isPhotoOrVideoSwitch.isChecked) {
                            val videoView = VideoView(this)
                            videoView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            videoView.setVideoURI(uri)
                            videoView.foregroundGravity = View.TEXT_ALIGNMENT_CENTER
                            videoView.setMediaController(MediaController(this))
                            videoView.start()
                            videoView.setZOrderOnTop(true)
                            binding.preview.addView(videoView)
                        } else {
                            val imageView = ImageView(this)
                            imageView.layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            imageView.setImageURI(uri)
                            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                            imageView.adjustViewBounds = true
                            binding.preview.addView(imageView)
                        }
                    }
                } else {
                    alerts.shortSimpleSnackbar(
                        binding.root,
                        "No se selecciono ningun archivo de la galeria"
                    )
                }
            }


        }
    }

    private fun startGallery() {
        val intentPick = Intent(Intent.ACTION_PICK)
        intentPick.type = if (binding.isPhotoOrVideoSwitch.isChecked) "video/*" else "image/*"
        startActivityForResult(intentPick, REQUEST_PICK)
    }

}