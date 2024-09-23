package com.example.gifts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class GiftAdapter extends ArrayAdapter<Gift> {

    private FirebaseFirestore db;
    private String loggedInUsername;

    public GiftAdapter(Context context, List<Gift> gifts, String loggedInUsername) {
        super(context, 0, gifts);
        db = FirebaseFirestore.getInstance();
        this.loggedInUsername = loggedInUsername;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Gift gift = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_gift, parent, false);
        }

        TextView textViewGiftName = convertView.findViewById(R.id.textViewGiftName);
        TextView textViewGiftLink = convertView.findViewById(R.id.textViewGiftLink);
        TextView textViewGiftStore = convertView.findViewById(R.id.textViewGiftStore);
        CheckBox checkBox = convertView.findViewById(R.id.checkBoxGift);

        textViewGiftName.setText(gift.getName());
        textViewGiftLink.setText(gift.getLink().isEmpty() ? "N/A" : gift.getLink());
        textViewGiftStore.setText(gift.getStore().isEmpty() ? "N/A" : gift.getStore());

        checkReservationStatus(gift, checkBox);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            gift.setReserved(isChecked);
            updateGiftReservation(gift, isChecked);
        });

        return convertView;
    }

    private void checkReservationStatus(Gift gift, CheckBox checkBox) {
        db.collection("users").document(loggedInUsername)
                .collection("gifts")
                .whereEqualTo("name", gift.getName())
                .whereEqualTo("link", gift.getLink())
                .whereEqualTo("store", gift.getStore())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Boolean isReserved = document.getBoolean("isReserved");
                        gift.setReserved(isReserved != null && isReserved);
                        checkBox.setChecked(gift.isReserved());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading reservation status", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateGiftReservation(Gift gift, boolean isReserved) {
        db.collection("users").document(loggedInUsername)
                .collection("gifts")
                .whereEqualTo("name", gift.getName())
                .whereEqualTo("link", gift.getLink())
                .whereEqualTo("store", gift.getStore())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("isReserved", isReserved)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Gift updated successfully", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Error updating gift", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading gifts", Toast.LENGTH_SHORT).show();
                });
    }
}
