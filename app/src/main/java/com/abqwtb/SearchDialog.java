package com.abqwtb;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class SearchDialog extends DialogFragment {

  private View view;
  private LayoutInflater inflater;
  private AlertDialog alertDialog;

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    // Get the layout inflater

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    if (view == null) {
      inflater = getActivity().getLayoutInflater();
      view = inflater.inflate(R.layout.search_dialog, null);
    }
    if (alertDialog == null) {
      final Activity activity = getActivity();
      AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setView(view)
          // Add action buttons
          .setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              ((SearchDialogListener) activity).onSearch(SearchDialog.this);
            }
          })
          .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
              SearchDialog.this.getDialog().cancel();
            }
          });
      alertDialog = builder.create();
    }
    return alertDialog;
  }

  public interface SearchDialogListener {

    void onSearch(DialogFragment dialog);
  }
}
