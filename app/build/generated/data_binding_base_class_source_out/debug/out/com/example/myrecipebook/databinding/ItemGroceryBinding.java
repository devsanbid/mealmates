// Generated by view binder compiler. Do not edit!
package com.example.myrecipebook.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

public final class ItemGroceryBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final CheckBox groceryItemCheckbox;

  @NonNull
  public final TextView groceryItemName;

  private ItemGroceryBinding(@NonNull LinearLayout rootView, @NonNull CheckBox groceryItemCheckbox,
      @NonNull TextView groceryItemName) {
    this.rootView = rootView;
    this.groceryItemCheckbox = groceryItemCheckbox;
    this.groceryItemName = groceryItemName;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemGroceryBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemGroceryBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_grocery, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemGroceryBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.grocery_item_checkbox;
      CheckBox groceryItemCheckbox = ViewBindings.findChildViewById(rootView, id);
      if (groceryItemCheckbox == null) {
        break missingId;
      }

      id = R.id.grocery_item_name;
      TextView groceryItemName = ViewBindings.findChildViewById(rootView, id);
      if (groceryItemName == null) {
        break missingId;
      }

      return new ItemGroceryBinding((LinearLayout) rootView, groceryItemCheckbox, groceryItemName);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
