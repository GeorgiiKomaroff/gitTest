package ru.mobile.beerhoven.data.repository;

import static java.util.Objects.requireNonNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.mobile.beerhoven.domain.model.Product;
import ru.mobile.beerhoven.utils.Constants;

public class CartRepository {
   private final List<Product> mDataList;
   private final MutableLiveData<List<Product>> mMutableList;
   private final String UID;
   private final DatabaseReference mFirebaseRef;

   public CartRepository() {
      this.mDataList = new ArrayList<>();
      this.mMutableList = new MutableLiveData<>();
      this.UID = requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
      this.mFirebaseRef = FirebaseDatabase.getInstance().getReference();
   }

   public MutableLiveData<List<Product>> getCartMutableList() {
      if (mDataList.size() == 0) {
         readCartList();
      }
      mMutableList.setValue(mDataList);
      return mMutableList;
   }

   // Read cart product list
   private void readCartList() {
      assert UID != null;
      mFirebaseRef.child(Constants.NODE_CART).child(UID).addChildEventListener(new ChildEventListener() {
         @Override
         public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Product order = dataSnapshot.getValue(Product.class);
            assert order != null;
            order.setId(dataSnapshot.getKey());
            if (!mDataList.contains(order)) {
               mDataList.add(order);
            }
            mMutableList.postValue(mDataList);
         }

         @Override
         public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Product order = dataSnapshot.getValue(Product.class);
            assert order != null;
            order.setId(dataSnapshot.getKey());
            if (mDataList.contains(order)) {
               mDataList.set(mDataList.indexOf(order), order);
            }
            mMutableList.postValue(mDataList);
         }

         @Override
         public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            Product order = dataSnapshot.getValue(Product.class);
            assert order != null;
            order.setId(dataSnapshot.getKey());
            mDataList.remove(order);
            mMutableList.postValue(mDataList);
         }

         @Override
         public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

         @Override
         public void onCancelled(@NonNull DatabaseError databaseError) {}
      });
   }

   // Delete cart list item by position
   public void deleteCartItem(String position) {
      assert UID != null;
      mFirebaseRef.child(Constants.NODE_CART).child(UID).child(position).removeValue();
   }
}
