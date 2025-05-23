// Generated by view binder compiler. Do not edit!
package com.example.myrecipebook.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.myrecipebook.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ListItemEditableBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ImageView deleteButton;

  @NonNull
  public final TextView itemText;

  private ListItemEditableBinding(@NonNull LinearLayout rootView, @NonNull ImageView deleteButton,
      @NonNull TextView itemText) {
    this.rootView = rootView;
    this.deleteButton = deleteButton;
    this.itemText = itemText;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ListItemEditableBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ListItemEditableBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.list_item_editable, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ListItemEditableBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.delete_button;
      ImageView deleteButton = ViewBindings.findChildViewById(rootView, id);
      if (deleteButton == null) {
        break missingId;
      }

      id = R.id.item_text;
      TextView itemText = ViewBindings.findChildViewById(rootView, id);
      if (itemText == null) {
        break missingId;
      }

      return new ListItemEditableBinding((LinearLayout) rootView, deleteButton, itemText);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
