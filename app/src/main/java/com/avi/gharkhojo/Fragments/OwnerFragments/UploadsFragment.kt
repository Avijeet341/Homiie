package com.avi.gharkhojo.Fragments.OwnerFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.avi.gharkhojo.Adapter.UploadsAdapter
import com.avi.gharkhojo.Model.Post
import com.avi.gharkhojo.Model.Upload
import com.avi.gharkhojo.databinding.FragmentUploadsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UploadsFragment : Fragment() {

    private lateinit var binding: FragmentUploadsBinding
    private lateinit var uploadsAdapter: UploadsAdapter
    private var databaseReference: DatabaseReference? = FirebaseDatabase.getInstance().reference.child("Posts")
        .child(FirebaseAuth.getInstance().currentUser?.uid!!)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadsAdapter = UploadsAdapter { post ->
            val action = UploadsFragmentDirections.actionUploadsFragmentToOwnerDetailFragment()
            var bundle = Bundle()
            bundle.putParcelable("post",post)
            action.arguments.putAll(bundle)
            findNavController().navigate(action)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = uploadsAdapter
        }
        CoroutineScope(Dispatchers.Main).launch {
            binding.postLoading.visibility = View.VISIBLE
            fetchAndUploadData()
        }


    }

    suspend fun fetchAndUploadData() = withContext(Dispatchers.IO) {
        try {
            val snapshot = databaseReference?.get()?.await()
            if (snapshot != null && snapshot.exists()) {
                val tempList = mutableListOf<Post>()
                    for (dataSnapshot in snapshot.children) {
                        val post = dataSnapshot.getValue(Post::class.java)
                        if (post != null) {
                            tempList.add(post)
                        }
                    }
                withContext(Dispatchers.Main) {
                    binding.postLoading.visibility = View.GONE
                    uploadsAdapter.updateData(tempList)
                    Log.d("size", "Fetched upload list size: ${tempList.size}")
                }
            } else {
                binding.postLoading.visibility = View.GONE
                Log.d("fetchAndUploadData", "Snapshot is null or empty")
            }
        } catch (e: Exception) {
            binding.postLoading.visibility = View.GONE
            Log.e("fetchAndUploadData", "Error fetching data", e)
        }
    }


}
