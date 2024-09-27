package com.example.gifts;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class GiftAdapter extends ArrayAdapter<Gift> {

    private FirebaseFirestore db;
    private String loggedInUsername;

    public GiftAdapter(Context context, List<Gift> gifts, String loggedInUsername) {
        super(context, R.layout.item_gift, gifts);
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
        Button buttonReserve = convertView.findViewById(R.id.buttonReserve);

        textViewGiftName.setText(gift.getName());
        textViewGiftLink.setText(gift.getLink().isEmpty() ? "Няма" : gift.getLink());
        textViewGiftStore.setText(gift.getStore().isEmpty() ? "Няма" : gift.getStore());

        checkReservationStatus(gift, buttonReserve);

        buttonReserve.setText(gift.isReserved() ? "Зарэзервіравана" : "Зарэзерваваць");
        buttonReserve.setEnabled(!gift.isReserved());

        buttonReserve.setOnClickListener(v -> {
            gift.setReserved(true);
            Log.d("GiftAdapter", "Рэзерваванне падарунка: " + gift.getName());
            updateGiftReservation(gift, true);
            buttonReserve.setText("Зарэзервіравана");
            buttonReserve.setEnabled(false);
        });

        return convertView;
    }

    private void checkReservationStatus(Gift gift, Button buttonReserve) {
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
                        buttonReserve.setText(gift.isReserved() ? "Зарэзервіравана" : "Зарэзерваваць");
                        buttonReserve.setEnabled(!gift.isReserved());
                    }
                })
                .addOnFailureListener(e -> {
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
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Падарунак не быў зарэзервіраваны", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Няма падарункаў", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void clear() {
        super.clear();
    }

    public void addAll(List<Gift> gifts) {
        super.addAll(gifts);
    }
}
