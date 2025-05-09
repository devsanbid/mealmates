// Generated by view binder compiler. Do not edit!
package com.example.myrecipebook.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import com.example.myrecipebook.R;
import java.lang.NullPointerException;
import java.lang.Override;

public final class ItemWeeklyPlanDayBinding implements ViewBinding {
  @NonNull
  private final TextView rootView;

  @NonNull
  public final TextView dayHeaderTextview;

  private ItemWeeklyPlanDayBinding(@NonNull TextView rootView,
      @NonNull TextView dayHeaderTextview) {
    this.rootView = rootView;
    this.dayHeaderTextview = dayHeaderTextview;
  }

  @Override
  @NonNull
  public TextView getRoot() {
    return rootView;
  }

  @NonNull
  public static ItemWeeklyPlanDayBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ItemWeeklyPlanDayBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.item_weekly_plan_day, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ItemWeeklyPlanDayBinding bind(@NonNull View rootView) {
    if (rootView == null) {
      throw new NullPointerException("rootView");
    }

    TextView dayHeaderTextview = (TextView) rootView;

    return new ItemWeeklyPlanDayBinding((TextView) rootView, dayHeaderTextview);
  }
}
