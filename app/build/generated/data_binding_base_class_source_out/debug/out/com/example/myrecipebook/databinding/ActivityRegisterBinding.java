// Generated by view binder compiler. Do not edit!
package com.example.myrecipebook.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.myrecipebook.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityRegisterBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LinearLayout linearLayout1;

  @NonNull
  public final TextView notYetRegister;

  @NonNull
  public final Button registerButton;

  @NonNull
  public final EditText registerEmail;

  @NonNull
  public final EditText registerName;

  @NonNull
  public final EditText registerPassword;

  @NonNull
  public final EditText registerUsername;

  @NonNull
  public final TextView textView3;

  @NonNull
  public final TextView textView4;

  private ActivityRegisterBinding(@NonNull ConstraintLayout rootView,
      @NonNull LinearLayout linearLayout1, @NonNull TextView notYetRegister,
      @NonNull Button registerButton, @NonNull EditText registerEmail,
      @NonNull EditText registerName, @NonNull EditText registerPassword,
      @NonNull EditText registerUsername, @NonNull TextView textView3,
      @NonNull TextView textView4) {
    this.rootView = rootView;
    this.linearLayout1 = linearLayout1;
    this.notYetRegister = notYetRegister;
    this.registerButton = registerButton;
    this.registerEmail = registerEmail;
    this.registerName = registerName;
    this.registerPassword = registerPassword;
    this.registerUsername = registerUsername;
    this.textView3 = textView3;
    this.textView4 = textView4;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityRegisterBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_register, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityRegisterBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.linearLayout1;
      LinearLayout linearLayout1 = ViewBindings.findChildViewById(rootView, id);
      if (linearLayout1 == null) {
        break missingId;
      }

      id = R.id.not_yet_register;
      TextView notYetRegister = ViewBindings.findChildViewById(rootView, id);
      if (notYetRegister == null) {
        break missingId;
      }

      id = R.id.register_button;
      Button registerButton = ViewBindings.findChildViewById(rootView, id);
      if (registerButton == null) {
        break missingId;
      }

      id = R.id.register_email;
      EditText registerEmail = ViewBindings.findChildViewById(rootView, id);
      if (registerEmail == null) {
        break missingId;
      }

      id = R.id.register_name;
      EditText registerName = ViewBindings.findChildViewById(rootView, id);
      if (registerName == null) {
        break missingId;
      }

      id = R.id.register_password;
      EditText registerPassword = ViewBindings.findChildViewById(rootView, id);
      if (registerPassword == null) {
        break missingId;
      }

      id = R.id.register_username;
      EditText registerUsername = ViewBindings.findChildViewById(rootView, id);
      if (registerUsername == null) {
        break missingId;
      }

      id = R.id.textView3;
      TextView textView3 = ViewBindings.findChildViewById(rootView, id);
      if (textView3 == null) {
        break missingId;
      }

      id = R.id.textView4;
      TextView textView4 = ViewBindings.findChildViewById(rootView, id);
      if (textView4 == null) {
        break missingId;
      }

      return new ActivityRegisterBinding((ConstraintLayout) rootView, linearLayout1, notYetRegister,
          registerButton, registerEmail, registerName, registerPassword, registerUsername,
          textView3, textView4);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
