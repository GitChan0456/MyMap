package com.example.Test;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Locale;

// 마커 클릭시 장소의 상세 정보를 표시하는 창
public class PlaceDetailDialogFragment extends DialogFragment {

    // --- ▼▼▼ 이 메서드를 추가합니다 ▼▼▼ ---
    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                // 1. 다이얼로그의 너비를 화면 너비에 꽉 채웁니다.
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // 2. 다이얼로그의 위치를 화면 하단으로 설정합니다.
                window.setGravity(Gravity.BOTTOM);

                // (선택 사항) 다이얼로그가 나타나고 사라지는 애니메이션을 부드럽게 만듭니다.
                // window.getAttributes().windowAnimations = R.style.DialogAnimation;

                // (선택 사항) 바텀 시트처럼 둥근 모서리를 적용하고 싶다면,
                // 다이얼로그의 기본 배경을 투명하게 만들어야 합니다.
                // window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
    }
    // --- ▲▲▲ 이 메서드를 추가합니다 ▲▲▲ ---
    // 데이터를 전달받기 위한 키(Key)
    private static final String ARG_PLACE_NAME = "place_name";
    private static final String ARG_PLACE_ADDRESS = "place_address";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    // 데이터를 받아 DialogFragment 인스턴스를 생성하는 정적 메서드 (가장 좋은 방법)
    public static PlaceDetailDialogFragment newInstance(String placeName, String placeAddress, double latitude, double longitude) {
        PlaceDetailDialogFragment fragment = new PlaceDetailDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PLACE_NAME, placeName);
        args.putString(ARG_PLACE_ADDRESS, placeAddress);
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML 레이아웃을 화면에 표시
        return inflater.inflate(R.layout.dialog_place_detail, container, false);
    }
// PlaceDetailDialogFragment.java

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI 요소 초기화
        TextView tvPlaceName = view.findViewById(R.id.tv_detail_name);
        TextView tvPlaceAddress = view.findViewById(R.id.tv_detail_address);
        TextView tvPlaceCoords = view.findViewById(R.id.tv_detail_coords);
        ImageButton btnClose = view.findViewById(R.id.btn_close);
        Button btnAddFavorite = view.findViewById(R.id.btn_add_favorite);
        Button btnSetDestination = view.findViewById(R.id.btn_set_destination);

        // newInstance를 통해 전달받은 데이터(arguments)를 가져옵니다.
        Bundle args = getArguments();
        if (args != null) {
            String placeName = args.getString(ARG_PLACE_NAME);
            String placeAddress = args.getString(ARG_PLACE_ADDRESS);
            double latitude = args.getDouble(ARG_LATITUDE);
            double longitude = args.getDouble(ARG_LONGITUDE);

            // 가져온 데이터로 TextView의 내용을 설정합니다.
            tvPlaceName.setText(placeName);
            tvPlaceAddress.setText(placeAddress);
            tvPlaceCoords.setText(String.format(Locale.KOREA, "위도: %.6f, 경도: %.6f", latitude, longitude));
        }

        // 닫기 버튼 클릭 리스너
        btnClose.setOnClickListener(v -> {
            dismiss(); // 다이얼로그를 닫습니다.
        });

        // 즐겨찾기 버튼 클릭 리스너
        btnAddFavorite.setOnClickListener(v -> {
            Toast.makeText(getContext(), "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
            // 여기에 실제 즐겨찾기 추가 로직 구현 예정
        });

        // 목적지 버튼 클릭 리스너
        btnSetDestination.setOnClickListener(v -> {
            Toast.makeText(getContext(), "목적지로 설정되었습니다.", Toast.LENGTH_SHORT).show();
            // 여기에 실제 목적지 설정 로직 구현 예정 (예: 경로 탐색 시작)
            dismiss(); // 목적지 설정 후 다이얼로그 닫기
        });
    }
}