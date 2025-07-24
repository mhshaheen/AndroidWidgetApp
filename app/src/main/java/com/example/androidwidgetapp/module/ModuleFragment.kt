package com.example.androidwidgetapp.module

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.androidwidgetapp.MyApplication
import com.example.androidwidgetapp.R
import com.example.androidwidgetapp.databinding.FragmentModuleBinding
import com.example.androidwidgetapp.databinding.ItemFileBinding
import com.ketch.DownloadModel
import com.ketch.Ketch
import com.ketch.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class ModuleFragment : Fragment() {

    private lateinit var fragmentModuleBinding: FragmentModuleBinding
    private lateinit var adapter: FilesAdapter
    private lateinit var ketch: Ketch

    companion object {
        fun newInstance(): ModuleFragment {
            val args = Bundle()
            val fragment = ModuleFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        ketch = (requireContext().applicationContext as MyApplication).ketch
        observer()
        fragmentModuleBinding = FragmentModuleBinding.inflate(inflater)
        return fragmentModuleBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FilesAdapter(object : FilesAdapter.FileClickListener {
            override fun onFileClick(downloadItem: DownloadModel) {
                if (downloadItem.status == Status.SUCCESS) {
                    val file = File(downloadItem.path, downloadItem.fileName)
                    if (file.exists()) {
                        val uri = this@ModuleFragment.context?.applicationContext?.let {
                            FileProvider.getUriForFile(
                                it,
                                it.packageName + ".provider",
                                file
                            )
                        }
                        if (uri != null) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, requireContext().contentResolver.getType(uri))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            try {
                                startActivity(intent)
                            } catch (ignore: Exception) {

                            }
                        }
                    } else {
                        Toast.makeText(
                            this@ModuleFragment.context,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCancelClick(downloadItem: DownloadModel) {
                ketch.cancel(downloadItem.id)
            }

            override fun onDownloadClick(downloadItem: DownloadModel) {
                ketch.download(
                    url = downloadItem.url,
                    fileName = downloadItem.fileName,
                    path = downloadItem.path,
                    tag = downloadItem.tag,
                    metaData = downloadItem.metaData
                )
            }

            override fun onPauseClick(downloadItem: DownloadModel) {
                ketch.pause(downloadItem.id)
            }

            override fun onResumeClick(downloadItem: DownloadModel) {
                ketch.resume(downloadItem.id)
            }

            override fun onRetryClick(downloadItem: DownloadModel) {
                ketch.retry(downloadItem.id)
            }

            override fun onDeleteClick(downloadItem: DownloadModel) {
                ketch.clearDb(downloadItem.id)
            }
        })
        fragmentModuleBinding.recyclerView.adapter = adapter
        (fragmentModuleBinding.recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations =
            false

        fragmentModuleBinding.recyclerView.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        fragmentModuleBinding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                this.context,
                DividerItemDecoration.VERTICAL
            )
        )

        fragmentModuleBinding.bt1.text = "Video 1"
        fragmentModuleBinding.bt1.setOnClickListener {
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_1.mp4",
                tag = "Video",
                metaData = "158"
            )
        }

        fragmentModuleBinding.bt2.text = "Video 2"
        fragmentModuleBinding.bt2.setOnClickListener {
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_2.mp4",
                tag = "Video",
                metaData = "169"
            )
        }

        fragmentModuleBinding.bt3.text = "Video 3"
        fragmentModuleBinding.bt3.setOnClickListener {
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_3.mp4",
                tag = "Video",
                metaData = "48"
            )
        }

        fragmentModuleBinding.bt4.text = "Image 1"
        fragmentModuleBinding.bt4.setOnClickListener {
            ketch.download(
                url = "https://picsum.photos/200/300",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Image_1.jpg",
                tag = "Document",
                metaData = "1"
            )
        }

        fragmentModuleBinding.bt5.text = "Pdf 1"
        fragmentModuleBinding.bt5.setOnClickListener {
            ketch.download(
                url = "https://sample-videos.com/pdf/Sample-pdf-5mb.pdf",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Pdf_1.pdf",
                tag = "Document",
                metaData = "5"
            )
        }

        fragmentModuleBinding.bt6.text = "Multiple"
        fragmentModuleBinding.bt6.setOnClickListener {
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_1.mp4",
                tag = "Video",
                metaData = "158"
            )
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_2.mp4",
                tag = "Video",
                metaData = "169"
            )
            ketch.download(
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_3.mp4",
                tag = "Video",
                metaData = "48"
            )
            ketch.download(
                url = "https://sample-videos.com/video321/mp4/720/big_buck_bunny_720p_30mb.mp4",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Video_4.mp4",
                tag = "Video",
                metaData = "30"
            )
            ketch.download(
                url = "https://picsum.photos/200/300",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Image_1.jpg",
                tag = "Document",
                metaData = "1"
            )
            ketch.download(
                url = "https://sample-videos.com/pdf/Sample-pdf-5mb.pdf",
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path,
                fileName = "Sample_Pdf_1.pdf",
                tag = "Document",
                metaData = "5"
            )
        }
    }

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ketch.observeDownloads()
                    .flowOn(Dispatchers.IO)
                    .collect {
                        adapter.submitList(it)
                    }
            }
        }
    }
}

class FilesAdapter(private val listener: FileClickListener) :
    ListAdapter<DownloadModel, FilesAdapter.ViewHolder>(
        DiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(downloadModel: DownloadModel) {
            binding.fileName.text = downloadModel.fileName
            binding.status.text = downloadModel.status.toString()
            binding.progressBar.progress = downloadModel.progress
            binding.progressText.text =
                downloadModel.progress.toString() + "%/" + Util.getTotalLengthText(downloadModel.total) + ", "
            binding.size.text = Util.getCompleteText(
                downloadModel.speedInBytePerMs,
                downloadModel.progress,
                downloadModel.total
            )

            binding.downloadButton.setOnClickListener {
                listener.onDownloadClick(downloadModel)
            }
            binding.cancelButton.setOnClickListener {
                listener.onCancelClick(downloadModel)
            }
            binding.pauseButton.setOnClickListener {
                listener.onPauseClick(downloadModel)
            }
            binding.resumeButton.setOnClickListener {
                listener.onResumeClick(downloadModel)
            }
            binding.deleteButton.setOnClickListener {
                listener.onDeleteClick(downloadModel)
            }
            binding.retryButton.setOnClickListener {
                listener.onRetryClick(downloadModel)
            }
            binding.root.setOnClickListener {
                listener.onFileClick(downloadModel)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DownloadModel>() {
        override fun areItemsTheSame(oldItem: DownloadModel, newItem: DownloadModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DownloadModel, newItem: DownloadModel): Boolean {
            return (oldItem == newItem)
        }

    }

    interface FileClickListener {
        fun onFileClick(downloadItem: DownloadModel)
        fun onCancelClick(downloadItem: DownloadModel)
        fun onDownloadClick(downloadItem: DownloadModel)
        fun onPauseClick(downloadItem: DownloadModel)
        fun onResumeClick(downloadItem: DownloadModel)
        fun onRetryClick(downloadItem: DownloadModel)
        fun onDeleteClick(downloadItem: DownloadModel)
    }
}