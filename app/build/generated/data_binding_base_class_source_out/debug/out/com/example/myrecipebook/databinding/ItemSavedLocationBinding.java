// Generated by view binder compiler. Do not edit!
package com.example.myrecipebook.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public final class ItemSavedLocationBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button buttonDirections;

  @NonNull
  public final TextView textLocation;

  @NonNull
  public final TextView textStoreName;

  private ItemSavedLocationBinding(@NonNull LinearLayout rootView, @NonNull Button buttonDirections,
      @NonNull TextView textLocation, @NonNull TextView textStoreName) {
    this.rootView = rootView;
    this.buttonDirections = buttonDirections;
    this.textLocation = textLocation;
    this.textStoreName = textStoreName;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemSavedLocationBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemSavedLocationBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_saved_location, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemSavedLocationBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.button_directions;
      Button buttonDirections = ViewBindings.findChildViewById(rootView, id);
      if (buttonDirections == null) {
        break missingId;
      }

      id = R.id.text_location;
      TextView textLocation = ViewBindings.findChildViewById(rootView, id);
      if (textLocation == null) {
        break missingId;
      }

      id = R.id.text_store_name;
      TextView textStoreName = ViewBindings.findChildViewById(rootView, id);
      if (textStoreName == null) {
        break missingId;
      }

      return new ItemSavedLocationBinding((LinearLayout) rootView, buttonDirections, textLocation,
          textStoreName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
