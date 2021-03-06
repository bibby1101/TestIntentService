package com.bibby.testintentservice;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.annotation.Nullable;

public class PairingFragment extends Fragment{
    private AppCompatTextView sectionLabel;
    private AppCompatTextView sectionIntro;
    private ImageView sectionImg;

    private int page;

    // The fragment argument representing
    // the section number for this fragment.
    private static final String ARG_SECTION_NUMBER = "section_number";

    public PairingFragment() {}

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PairingFragment newInstance(int sectionNumber) {
        PairingFragment fragment = new PairingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt(ARG_SECTION_NUMBER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pairing, container, false);

        initViews(view);

        switch (page) {
            case 0:
                sectionImg.setBackgroundResource(R.drawable.ic_notifications_black_24dp);
                sectionLabel.setText(R.string.onboarding_section_1_cht);
                sectionIntro.setText(R.string.onboarding_intro_1_cht);
                break;
//            case 1:
//                sectionImg.setBackgroundResource(R.drawable.ic_camera_black_24dp);
//                sectionLabel.setText(R.string.onboarding_section_2);
//                sectionIntro.setText(R.string.onboarding_intro_2);
//                break;
            case 1:
                sectionImg.setBackgroundResource(R.drawable.ic_beenhere_black_24dp);
                sectionLabel.setText(R.string.onboarding_section_3_cht);
                sectionIntro.setText(R.string.onboarding_intro_3_cht);
                break;
            default:
                break;
        }

        return view;
    }

    private void initViews(View view) {
        sectionLabel = (AppCompatTextView) view.findViewById(R.id.section_label);
        sectionIntro = (AppCompatTextView) view.findViewById(R.id.section_intro);
        sectionImg = (ImageView) view.findViewById(R.id.section_img);
    }

}
