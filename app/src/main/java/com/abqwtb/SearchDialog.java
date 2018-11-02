package com.abqwtb;

import static com.abqwtb.StopsListActivity.ROUTE_NUM;
import static com.abqwtb.StopsListActivity.STOP_ID;
import static com.abqwtb.StopsListActivity.STOP_NAME;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class SearchDialog extends DialogFragment {

  private View view;
  private LayoutInflater inflater;
  private AlertDialog alertDialog;

  public static SearchDialog newInstance(int stopId, int routeNum, String stopName) {

    Bundle args = new Bundle();

    args.putInt(STOP_ID, stopId);
    args.putInt(ROUTE_NUM, routeNum);
    args.putString(STOP_NAME, stopName);

    SearchDialog fragment = new SearchDialog();
    fragment.setArguments(args);
    return fragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    Bundle args = getArguments();

    // Get the layout inflater

    // Inflate and set the layout for the dialog
    // Pass null as the parent view because its going in the dialog layout
    if (view == null) {
      inflater = getActivity().getLayoutInflater();
      view = inflater.inflate(R.layout.search_dialog, null);
      if (args != null) {
        if (args.getInt(STOP_ID) > 0) {
          ((EditText) view.findViewById(R.id.stop_id_search))
              .setText(String.valueOf(args.getInt(STOP_ID)));
        }
        if (args.getInt(ROUTE_NUM) > 0) {
          ((EditText) view.findViewById(R.id.route_number_search))
              .setText(String.valueOf(args.getInt(ROUTE_NUM)));
        }
        ((EditText) view.findViewById(R.id.stop_name_search))
            .setText(args.getString(STOP_NAME));
      }
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
