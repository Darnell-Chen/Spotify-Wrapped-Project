package com.example.spotifywrapped;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DeleteAccount {

    private final Context context;

    public DeleteAccount(Context context) {
        this.context = context;
    }

    public void terminateAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ConfirmDeleteDialogFragment confirmDialog = new ConfirmDeleteDialogFragment();
                confirmDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "confirmDelete");
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void reAuthenticate(String email, String password, Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, password);

            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // On successful re-authentication, proceed to delete the account
                            deleteAccount(context);
                        } else {
                            Toast.makeText(context, "Re-authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private static void deleteAccount(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUser.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Account has been deleted", Toast.LENGTH_SHORT).show();
                    logout(context);
                } else {
                    Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private static void logout(Context context) {
        FirebaseAuth.getInstance().signOut();
        Intent loginPage = new Intent(context, MainActivity.class);
        loginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(loginPage);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    // DialogFragment for confirming account deletion with password
    public static class ConfirmDeleteDialogFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_confirm_delete, null);

            final EditText emailInput = view.findViewById(R.id.email);
            final EditText passwordInput = view.findViewById(R.id.password);

            builder.setView(view)
                    .setTitle("Enter Credentials to Confirm Account Deletion")
                    .setPositiveButton("Confirm", (dialog, id) -> {
                        String email = emailInput.getText().toString();
                        String password = passwordInput.getText().toString();

                        if (email.length() == 0 || password.length() == 0) {
                            Toast.makeText(getContext(), "No credentials entered", Toast.LENGTH_SHORT).show();
                        } else {
                            reAuthenticate(email, password, getContext());
                        }
                    });

            return builder.create();
        }
    }
}