package com.example.Test; // 본인 프로젝트의 패키지명

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.LocationOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.MarkerIcons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.xmlpull.v1.XmlPullParser; // import 추가
import org.xmlpull.v1.XmlPullParserFactory; // import 추가
import java.io.InputStream; // import 추가
import java.io.InputStreamReader; // import 추가
import java.net.URL; // import 추가

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    // --- [추가] 네이버 API 인증 정보 ---
    private static final String NAVER_CLIENT_ID = "2frdg56dcg";
    private static final String NAVER_CLIENT_SECRET = "9Z4Lti9BrbnfqxHVaIfgoRZpLIoypZCyV3lNy9NM";

    // OkHttp 클라이언트 변수 추가
    private OkHttpClient httpClient;

    // 네이버 지도 관련
    private MapFragment mapFragment; // MapFragment 참조
    private NaverMap naverMap;
    private Marker currentGeocodedMarker; // 지오코딩 결과로 표시된 마커
    private Marker longClickMarker; //지도 롱클릭 해당 장소 마커
    // UI 요소
    private EditText etAddress; //주소 입력창
    private Button btnGeocode;  //검색 버튼
    private FloatingActionButton btnCurrentLocation; // 현위치 버튼
    private SwitchMaterial switchSomeFeature;   // 스위치 변수 추가

    // 지오코딩 관련
    private Geocoder geocoder;

    // 위치 서비스 관련
    private FusedLocationProviderClient fusedLocationClient; // 위치 정보 제공 클라이언트
    private LocationRequest locationRequest; // 위치 요청 정보
    private LocationCallback locationCallback; // 위치 업데이트 콜백
    private LocationOverlay locationOverlay; // 지도 위 현재 위치 표시 오버레이

    // BIS 관련
    private static final String DATA_GO_KR_SERVICE_KEY = "ffM27vy9DGkDka9x8liDumAwewOhqFwXxQTsywa37yJnj5sC1gba%2FgxZhCjct2Ht27OR3uN6WO2To439x55fIA%3D%3D";
    private List<Marker> busStopMarkers = new ArrayList<>(); // 지도에 표시된 버스 정류장 마커들을 관리하기 위한 리스트
    private OverlayImage busIcon;   // 버스 아이콘 이미지 저장 객체
    private InfoWindow infoWindow; // 정류장의 버스 정보 출력할 창 변수 추가
    private static final double MIN_ZOOM_FOR_BUS_STOPS = 10.0; // 버스 정류장을 표시할 최소 줌 레벨
    private boolean isBusStopViewState = false; //switch on/off 상태 표현

    // 바텀시트 관련
    private LinearLayout btnFeature1, btnFeature2, btnFeature3, btnFeature4, btnFeature5, btnFeature6;
    private Button btnLogout; // 로그아웃 버튼 변수 추가

    //onCreate는 실행시 처음에 한번만 실행
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI 요소 초기화
        etAddress = findViewById(R.id.et_address);
        btnGeocode = findViewById(R.id.btn_geocode);
        btnCurrentLocation = findViewById(R.id.btn_current_location);
        switchSomeFeature = findViewById(R.id.switch_some_feature);

        // 바텀시트 기능별 버튼들
        View bottomSheet = findViewById(R.id.bottom_sheet_frame); // 바텀 시트 컨테이너
        btnFeature1 = bottomSheet.findViewById(R.id.btn_feature_1);
        btnFeature2 = bottomSheet.findViewById(R.id.btn_feature_2);
        btnFeature3 = bottomSheet.findViewById(R.id.btn_feature_3);
        btnFeature4 = bottomSheet.findViewById(R.id.btn_feature_4);
        btnFeature5 = bottomSheet.findViewById(R.id.btn_feature_5);
        btnFeature6 = bottomSheet.findViewById(R.id.btn_feature_6);
        btnLogout = bottomSheet.findViewById(R.id.logout_button);

        // 각 기능 버튼에 대한 클릭 리스너
        btnFeature1.setOnClickListener(v -> Toast.makeText(this, "길찾기 기능", Toast.LENGTH_SHORT).show());
        btnFeature2.setOnClickListener(v -> Toast.makeText(this, "주변장소 탐색", Toast.LENGTH_SHORT).show());
        btnFeature3.setOnClickListener(v -> Toast.makeText(this, "즐겨찾는 장소", Toast.LENGTH_SHORT).show());
        btnFeature4.setOnClickListener(v -> Toast.makeText(this, "기능 4 클릭됨", Toast.LENGTH_SHORT).show());
        btnFeature5.setOnClickListener(v -> Toast.makeText(this, "기능 5 클릭됨", Toast.LENGTH_SHORT).show());
        btnFeature6.setOnClickListener(v -> Toast.makeText(this, "기능 6 클릭됨", Toast.LENGTH_SHORT).show());
        // 로그아웃 버튼 클릭 리스너 설정
        btnLogout.setOnClickListener(v -> {
            // 로그아웃 로직 실행
            logoutAndGoToLogin();
        });

        // Geocoder 초기화 (한국 기준)
        geocoder = new Geocoder(this, Locale.KOREA);
        // FusedLocationProviderClient 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // OkHttp 클라이언트 초기화
        httpClient = new OkHttpClient();

        // MapFragment 초기화 및 비동기 콜백 등록
        FragmentManager fm = getSupportFragmentManager();
        mapFragment = (MapFragment) fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this); // 온맵레디 콜백을 설정

        // 검색 버튼 클릭 리스너
        btnGeocode.setOnClickListener(v -> {
            String addressText = etAddress.getText().toString();
            if (!addressText.isEmpty()) {
                performGeocoding(addressText);
            } else {
                Toast.makeText(MainActivity.this, "주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        // 현재 위치로 이동 버튼 클릭 리스너
        btnCurrentLocation.setOnClickListener(v -> {
            if (naverMap == null) {
                Toast.makeText(this, "지도가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            // 위치 권한 확인
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 권한이 있으면 마지막으로 알려진 위치를 가져와 카메라 이동
                requestAndMoveToCurrentLocation();
            }
            else // 권한이 없다면 권한 요청 (checkLocationPermission 내부에서 처리)
            {
                checkLocationPermission(); // 권한 요청
                Toast.makeText(this, "현재 위치를 보려면 위치 권한이 필요합니다. 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
            }
        });

        // 스위치의 체크 상태 변경 리스너 설정
        switchSomeFeature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // isChecked 변수에는 스위치의 새로운 상태가 true(On) 또는 false(Off)로 전달됩니다.
            if (isChecked) {
                // 스위치가 켜졌을 때
                isBusStopViewState = true;
                CameraPosition cameraPosition = naverMap.getCameraPosition();
                double currentZoom = cameraPosition.zoom;
                LatLng currentPosition = cameraPosition.target;

                if (currentZoom >= MIN_ZOOM_FOR_BUS_STOPS && isBusStopViewState == true) {
                    fetchAndDisplayBusStops(currentPosition);
                }
            } else {
                // 스위치가 꺼졌을 때
                isBusStopViewState = false;
                clearBusStopMarkers();
            }
        });

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d(TAG, "NaverMap is ready!");
        this.naverMap = naverMap;
        // LocationOverlay 객체 가져오기 (지도에 현재 위치를 표시하기 위함)
        this.locationOverlay = naverMap.getLocationOverlay();

        // --- ▼▼▼ 이 코드를 추가합니다 ▼▼▼ ---
        // 정보 창(InfoWindow) 객체 초기화
        this.infoWindow = new InfoWindow();
        // 정보 창 자체를 클릭하면 닫히도록 리스너 설정
        this.infoWindow.setOnClickListener(overlay -> {
            infoWindow.close();
            return true; // 이벤트 소비
        });
        // --- ▲▲▲ 이 코드를 추가합니다 ▲▲▲ ---

        // 초기 지도 위치 설정 (예: 충청북도청)
        LatLng initialPosition = new LatLng(36.6358083, 127.4913333);
        this.naverMap.moveCamera(CameraUpdate.scrollTo(initialPosition));

        // 지도 롱 클릭 리스너 (리버스 지오코딩용)
        this.naverMap.setOnMapLongClickListener((point, coord) -> {
            if (longClickMarker  != null) {
                longClickMarker .setMap(null); // 기존 마커 제거
            }
            longClickMarker  = new Marker();
            longClickMarker .setPosition(coord);
            longClickMarker .setMap(naverMap); // 새 마커 추가

            // --- ▼▼▼ 이 부분을 수정/추가합니다 ▼▼▼ ---
            // 장소의 정보를 나타내는 팝업창 띄우기(dialog_place_detail)
            new Thread(() -> {
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(coord.latitude, coord.longitude, 1);
                } catch (IOException e) {
                    Log.e(TAG, "Reverse Geocoding failed for click listener", e);
                }

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String placeName = address.getFeatureName() != null ? address.getFeatureName() : "이름 없는 장소"; // 장소 이름
                    String placeAddress = address.getAddressLine(0); // 전체 주소

                    runOnUiThread(() -> {
                        // 2. 가져온 주소 정보로 마커의 캡션을 설정합니다.
                        //longClickMarker.setCaptionText(placeName);

                        // 3. 마커에 클릭 리스너를 설정하여 다이얼로그를 띄웁니다.
                        longClickMarker.setOnClickListener(overlay -> {
                            PlaceDetailDialogFragment dialogFragment = PlaceDetailDialogFragment.newInstance(
                                    placeName,
                                    placeAddress,
                                    coord.latitude,
                                    coord.longitude
                            );
                            dialogFragment.show(getSupportFragmentManager(), "place_detail_dialog");
                            return true;
                        });
                    });
                }
            }).start();
            // --- ▲▲▲ 이 부분을 수정/추가합니다 ▲▲▲ ---

            performReverseGeocoding(coord); // 리버스 지오코딩 실행
        });

        // 지도 준비가 완료되고, 위치 권한이 이미 있다면 위치 업데이트 시작
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }

        // 카메라 이동이 멈추면 호출되는 리스너 설정
        naverMap.addOnCameraIdleListener(() -> {
            // 현재 카메라 위치와 줌 레벨을 가져옵니다.
            CameraPosition cameraPosition = naverMap.getCameraPosition();
            double currentZoom = cameraPosition.zoom;
            LatLng currentPosition = cameraPosition.target;

            Log.d(TAG, "카메라 이동 멈춤. 현재 줌 레벨: " + currentZoom);

            // 현재 줌 레벨이 설정한 기준보다 확대되었는지 확인, 정류장 표시 스위치가 켜진것을 확인
            if (currentZoom >= MIN_ZOOM_FOR_BUS_STOPS && isBusStopViewState == true) {
                fetchAndDisplayBusStops(currentPosition);
            } else {
                // 기준보다 축소되었다면: 기존에 있던 버스 정류장 마커를 모두 지웁니다.
                runOnUiThread(this::clearBusStopMarkers);
            }
        });


    }

    // 위치 권한 확인 권한이 없다면 권한 요청창 띄우기
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우, 사용자에게 권한 요청
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 권한이 이미 있는 경우 (또는 여기서 startLocationUpdates를 호출하지 않고 onMapReady에서 처리)
            if (naverMap != null) { // 지도가 준비된 후에 위치 업데이트 시작
                startLocationUpdates();
            }
        }
    }

    // 사용자 권한 요청 처리 결과 알림 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 사용자가 권한을 승인한 경우
                Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
                if (naverMap != null) { // 지도가 준비된 후에 위치 업데이트 시작
                    startLocationUpdates();
                }
            } else {
                // 사용자가 권한을 거부한 경우
                Toast.makeText(this, "위치 권한이 거부되었습니다. 현재 위치 기능을 사용하려면 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // 위치 업데이트 시작 메소드
    private void startLocationUpdates() {
        // 권한 재확인 (필수)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.w(TAG, "Location permission not granted before starting updates.");
            return;
        }

        // LocationRequest 설정 (한 번만 생성)
        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // 업데이트 간격: 10초
                    .setMinUpdateIntervalMillis(5000) // 최소 업데이트 간격: 5초
                    .build();
        }

        // LocationCallback 정의 (한 번만 생성)
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null && naverMap != null && locationOverlay != null) {
                            Log.d(TAG, "Current Location Update: " + location.getLatitude() + ", " + location.getLongitude());
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            // LocationOverlay를 사용하여 현재 위치 표시
                            locationOverlay.setVisible(true);
                            locationOverlay.setPosition(currentLatLng);
                            locationOverlay.setBearing(location.getBearing()); // 현재 위치 방향 (선택 사항)
                        }
                    }
                }
            };
        }
        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d(TAG, "Requesting location updates.");
    }

    // 위치 업데이트 중지 메소드
    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates stopped.");
        }
    }

    // 현재 위치로 이동 버튼 클릭 시 호출될 메소드
    private void requestAndMoveToCurrentLocation() {
        // 권한 확인 (필수)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "현재 위치를 가져오려면 위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 마지막으로 알려진 위치 가져오기
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null && naverMap != null) //
                    {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d(TAG, "Moving to current location: " + currentLatLng.latitude + ", " + currentLatLng.longitude);

                        // --- [요청 사항] Toast 메시지 추가 ---
                        String message = "위도: " + location.getLatitude() + "\n경도: " + location.getLongitude();
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        // ------------------------------------

                        if (locationOverlay != null) {
                            locationOverlay.setVisible(true);
                            locationOverlay.setPosition(currentLatLng);
                            // getLastLocation은 bearing 정보를 항상 제공하지 않을 수 있으므로, 여기서는 bearing 설정 생략 가능
                        }
                        // 현재 위치로 카메라 이동 및 줌 레벨 설정
                        naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng));
                        naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng).zoomTo(15));
                    }
                    else
                    {
                        Log.d(TAG, "Last known location is null. Requesting new location update for camera move.");
                        // 마지막 위치가 없는 경우, LocationOverlay에 이미 위치가 설정되어 있다면 그곳으로 이동
                        if (locationOverlay != null && locationOverlay.getPosition() != null) {
                            naverMap.moveCamera(CameraUpdate.scrollTo(locationOverlay.getPosition()));
                            naverMap.moveCamera(CameraUpdate.scrollTo(locationOverlay.getPosition()).zoomTo(15));
                        } else {
                            Toast.makeText(MainActivity.this, "현재 위치를 가져올 수 없습니다. GPS를 확인하거나 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "Error getting last location", e);
                    Toast.makeText(MainActivity.this, "위치 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    // --- 지오코딩 / 리버스 지오코딩 메소드 (이전 단계에서 추가한 내용) ---
    private void performGeocoding(String addressString) {
        new Thread(() -> {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocationName(addressString, 1);
            }
            catch (IOException e) {
                Log.e(TAG, "Geocoding IOException: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "지오코딩 서비스 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                return;
            }

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng point = new LatLng(address.getLatitude(), address.getLongitude()); //좌표객체
                String snippet = address.getAddressLine(0);
                Log.d(TAG, "Geocoded Address: " + snippet + ", Lat: " + point.latitude + ", Lng: " + point.longitude);

                runOnUiThread(() -> {
                    if (naverMap != null) {
                        if (currentGeocodedMarker != null) {
                            currentGeocodedMarker.setMap(null);
                        }
                        currentGeocodedMarker = new Marker();
                        currentGeocodedMarker.setPosition(point);
                        currentGeocodedMarker.setIcon(MarkerIcons.BLUE);
                        currentGeocodedMarker.setCaptionText(addressString.length() > 15 ? addressString.substring(0,15)+"..." : addressString);
                        currentGeocodedMarker.setMap(naverMap);

                        // 생성된 마커에 클릭 리스너를 설정합니다.
                        currentGeocodedMarker.setOnClickListener(overlay -> {
                            // 1. newInstance 메서드를 사용하여 데이터를 담은 다이얼로그 프래그먼트를 생성합니다.
                            PlaceDetailDialogFragment dialogFragment = PlaceDetailDialogFragment.newInstance(
                                    addressString,      // 장소 이름
                                    snippet,            // 상세 주소
                                    point.latitude,     // 위도
                                    point.longitude     // 경도
                            );

                            // 2. FragmentManager를 사용하여 다이얼로그를 화면에 표시합니다.
                            dialogFragment.show(getSupportFragmentManager(), "place_detail_dialog");

                            return true; // 이벤트 소비
                        });


                        naverMap.moveCamera(CameraUpdate.scrollTo(point));                  //카메라 위치 지정
                        naverMap.moveCamera(CameraUpdate.scrollTo(point).zoomTo(15));       //카메라 줌 정도
                        Toast.makeText(MainActivity.this, "검색 결과: " + snippet, Toast.LENGTH_LONG).show();
                        //etAddress.setText(""); // 검색 후 입력창 비우기

                        if (locationOverlay != null && locationOverlay.getPosition() != null)
                        {
                            LatLng startPoint = locationOverlay.getPosition(); // 현재 위치를 출발지로
                            LatLng goalPoint = point; // 검색된 위치를 목적지로
                            //requestNaverDirections(startPoint, goalPoint); // 경로 요청!
                        }
                        else
                        {
                            Toast.makeText(this, "경로를 탐색하려면 현재 위치 정보가 필요합니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                Log.d(TAG, "No address found for: " + addressString);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "해당 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void performReverseGeocoding(LatLng latLng) {
        new Thread(() -> {
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            } catch (IOException e) {
                Log.e(TAG, "Reverse Geocoding IOException: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "리버스 지오코딩 서비스 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
                return;
            }

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = address.getAddressLine(0);
                Log.d(TAG, "Reverse Geocoded Address: " + addressText);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "선택한 위치 주소: " + addressText, Toast.LENGTH_LONG).show();
                    if (longClickMarker  != null) { // 롱클릭으로 생성된 마커 하단에 주소 표시
                        //이것은 나중에
                        //longClickMarker .setCaptionText(addressText.length() > 20 ? addressText.substring(0,20)+"..." : addressText);
                    }
                });
            } else {
                Log.d(TAG, "No address found for coordinates: " + latLng.latitude + ", " + latLng.longitude);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "해당 위치의 주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void requestNaverDirections(LatLng start, LatLng goal) {
        Log.d(TAG, "requestNaverDirections 호출됨: start=" + start.toString() + ", goal=" + goal.toString());

        // API 키가 설정되었는지 확인
        if (NAVER_CLIENT_ID.equals("YOUR_CLIENT_ID") || NAVER_CLIENT_SECRET.equals("YOUR_CLIENT_SECRET")) {
            runOnUiThread(() -> Toast.makeText(this, "API 키를 설정해주세요.", Toast.LENGTH_LONG).show());
            return;
        }

        // 네이버 Directions API v1 (driving) URL 구성
        String urlString = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving" +
                "?start=" + start.longitude + "," + start.latitude +
                "&goal=" + goal.longitude + "," + goal.latitude;
        // "&option=trafast"; // 예: 빠른길 옵션 (선택 사항)

        // OkHttp를 사용하여 비동기 네트워크 요청
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url(urlString)
                    .addHeader("X-NCP-APIGW-API-KEY-ID", NAVER_CLIENT_ID)
                    .addHeader("X-NCP-APIGW-API-KEY", NAVER_CLIENT_SECRET)
                    .build();

            try {
                Response response = httpClient.newCall(request).execute(); // 동기 방식 요청

                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    Log.d(TAG, "Directions API 성공: " + responseBody);

                    // 성공 토스트 메시지 (UI 스레드에서 실행)
                    runOnUiThread(() -> Toast.makeText(this, "경로 정보를 성공적으로 받아왔습니다.", Toast.LENGTH_SHORT).show());

                    // 다음 단계: 여기서 JSON을 파싱하고 경로선을 그립니다.
                    // parseAndDrawRoute(responseBody);

                } else {
                    String errorBody = response.body().string();
                    Log.e(TAG, "Directions API 실패: " + response.code() + ", " + errorBody);

                    runOnUiThread(() -> Toast.makeText(this, "경로 탐색 실패: " + response.code(), Toast.LENGTH_SHORT).show());
                }

            } catch (IOException e) {
                Log.e(TAG, "Directions API IOException", e);
                runOnUiThread(() -> Toast.makeText(this, "경로 탐색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void fetchAndDisplayBusStops(LatLng center) {
        // 서비스 키가 설정되지 않았다면 메서드 종료
        if (DATA_GO_KR_SERVICE_KEY.equals("YOUR_DATA_GO_KR_SERVICE_KEY")) {
            // 처음 한 번만 Toast 메시지를 띄우거나, Log만 남길 수 있습니다.
            // Toast.makeText(this, "공공데이터 포털 서비스 키를 설정해주세요.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "공공데이터 포털 서비스 키가 설정되지 않았습니다.");
            return;
        }

        // 아이콘 이미지가 아직 로드되지 않았다면 로드합니다.
        if (busIcon == null) {
            busIcon = OverlayImage.fromResource(R.drawable.ic_directions_bus);
        }

        // 네트워크 작업은 반드시 별도의 스레드에서 수행해야 합니다.
        new Thread(() -> {
            // 이전에 표시된 버스 정류장 마커들을 지도에서 제거하고 리스트를 비웁니다.
            runOnUiThread(() -> {
                for (Marker marker : busStopMarkers) {
                    marker.setMap(null);
                }
                busStopMarkers.clear();
            });

            try {
                // 1. API 요청을 위한 URL 생성 (좌표기반 근접 정류소 목록조회)
                StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getCrdntPrxmtSttnList");
                urlBuilder.append("?serviceKey=").append(DATA_GO_KR_SERVICE_KEY);
                urlBuilder.append("&pageNo=").append("1");
                urlBuilder.append("&numOfRows=").append("30"); // 한 번에 최대 30개 정류장 정보 요청
                urlBuilder.append("&_type=").append("xml");
                urlBuilder.append("&gpsLati=").append(center.latitude);
                urlBuilder.append("&gpsLong=").append(center.longitude);

                URL url = new URL(urlBuilder.toString());
                InputStream is = url.openStream(); // URL에 연결하여 데이터 스트림을 엽니다.

                // 2. XML 파서(Parser)를 사용하여 데이터 분석
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(is, "UTF-8")); // 스트림과 인코딩 설정

                String tag;
                String stationName = null, stationId = null, stationNo = null;
                double lat = 0, lng = 0;
                int eventType = xpp.getEventType();

                // XML 문서가 끝날 때까지 반복
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) { // 시작 태그를 만났을 때
                        tag = xpp.getName();

                        // 각 태그 이름에 따라 데이터 추출
                        if (tag.equals("nodenm")) { // 정류소명
                            xpp.next();
                            stationName = xpp.getText();
                        } else if (tag.equals("nodeno")) { // 정류소번호
                            xpp.next();
                            stationNo = xpp.getText();
                        }
                        else if (tag.equals("nodeid")) { // 정류소id
                            xpp.next();
                            stationId = xpp.getText();
                            Log.d(TAG,"name, id, no = " + stationName + ", " + stationId + ", " + stationNo );
                        }
                        else if (tag.equals("gpslati")) { // 위도
                            xpp.next();
                            lat = Double.parseDouble(xpp.getText());
                        } else if (tag.equals("gpslong")) { // 경도
                            xpp.next();
                            lng = Double.parseDouble(xpp.getText());
                        }
                    } else if (eventType == XmlPullParser.END_TAG) { // 종료 태그를 만났을 때
                        tag = xpp.getName();
                        // <item> 태그가 끝나면 하나의 정류장 정보가 완성된 것이므로 마커를 생성합니다.
                        if (tag.equals("item") && stationName != null) {
                            final String finalStationName = stationName;
                            final String finalStationId = stationId;
                            final LatLng stationPosition = new LatLng(lat, lng);

                            // 3. 마커를 지도에 표시 (UI 작업이므로 UI 스레드에서 실행)
                            runOnUiThread(() -> {
                                Marker marker = new Marker();
                                marker.setPosition(stationPosition);
                                marker.setCaptionText(finalStationName);
                                marker.setIcon(busIcon);
                                marker.setWidth(60); // 아이콘 크기
                                marker.setHeight(60);
                                marker.setTag(finalStationId); // 나중에 도착정보 조회를 위해 정류장 ID를 태그로 저장
                                marker.setMap(naverMap);
                                busStopMarkers.add(marker); // 화면에 표시된 마커들을 관리하기 위해 리스트에 추가

                                // --- ▼▼▼ 이 부분을 추가합니다 ▼▼▼ ---
                                // 각 마커에 클릭 리스너를 설정합니다.
                                marker.setOnClickListener(overlay -> {
                                    // 클릭된 마커를 전달하며 정보 표시 메서드 호출
                                    showArrivalInfo((Marker) overlay);
                                    return true; // true를 반환하면 이벤트가 소비되어 지도의 클릭 리스너는 호출되지 않음
                                });
                                // --- ▲▲▲ 이 부분을 추가합니다 ▲▲▲ ---
                            });
                        }
                    }
                    eventType = xpp.next(); // 다음 이벤트로 이동
                }

            } catch (Exception e) {
                Log.e(TAG, "Error during fetchAndDisplayBusStops", e);
                runOnUiThread(() -> Toast.makeText(this, "힝 버스 정류장 정보를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void clearBusStopMarkers() {
        for (Marker marker : busStopMarkers) {
            marker.setMap(null); // 각 마커를 지도에서 제거
        }
        busStopMarkers.clear(); // 마커 리스트 비우기
    }

    private void logoutAndGoToLogin() {
        // 1. LoginActivity로 이동하기 위한 Intent 생성
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);

        // 2. [매우 중요] 기존의 모든 액티비티를 스택에서 제거하는 플래그 설정
        // 이렇게 해야 로그인 화면으로 이동한 뒤, 뒤로가기 버튼을 눌렀을 때
        // 이전 화면(지도 화면)으로 돌아가지 않고 앱이 종료됩니다.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 3. LoginActivity 시작
        startActivity(intent);

        // 4. 현재 MainActivity 즉시 종료
        finish();

        // 5. 로그아웃 완료 Toast 메시지 표시
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
    }
// MainActivity.java 내부에 아래 메서드 전체를 추가합니다.

    /**
     * 마커를 클릭했을 때, 도착 정보를 가져와 정보 창에 표시하는 전체 과정을 관리합니다.
     * @param marker 사용자가 클릭한 마커 객체
     */
    private void showArrivalInfo(Marker marker) {
        // 마커에 저장된 정류장 ID를 가져옵니다.
        String stationId = (String) marker.getTag();
        Log.d(TAG,"메소드 showArrivalInfo()의 정류소ID: "+ stationId );
        if (stationId == null) return;

        // 정보 창에 "로딩 중..." 메시지를 먼저 표시하여 사용자에게 피드백을 줍니다.
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                return "도착 정보를 불러오는 중...";
            }
        });
        infoWindow.open(marker); // 마커 위에 로딩 중 메시지를 띄움

        // 네트워크 작업은 별도의 스레드에서 수행합니다.
        new Thread(() -> {
            // 실제 도착 정보를 API로 받아옵니다.
            final String arrivalInfo = fetchBusArrivals(stationId);

            // 최종 결과를 UI 스레드에서 정보 창의 내용으로 업데이트합니다.
            runOnUiThread(() -> {
                if (infoWindow.getMarker() == marker) {
                    infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
                        @NonNull
                        @Override
                        public CharSequence getText(@NonNull InfoWindow infoWindow) {
                            return arrivalInfo; // 최종 도착 정보 텍스트
                        }
                    });
                    // 어댑터가 바뀌었으므로 다시 open()을 호출하여 내용을 갱신할 수 있습니다.
                    infoWindow.open(marker);
                }
            }
            );
        }).start();
    }
    // MainActivity.java

    private String fetchBusArrivals(String stationId) {
        // OkHttp 클라이언트는 이미 onCreate에서 초기화되었습니다 (httpClient)

        // API 요청 URL 구성. OkHttp의 HttpUrl.Builder를 사용하면 파라미터가 안전하게 인코딩됩니다.
        okhttp3.HttpUrl.Builder urlBuilder = okhttp3.HttpUrl.parse("http://apis.data.go.kr/1613000/ArvlInfoInqireService/getSttnAcctoArvlPrearngeInfoList").newBuilder();
        urlBuilder.addEncodedQueryParameter ("serviceKey", DATA_GO_KR_SERVICE_KEY);
        urlBuilder.addEncodedQueryParameter ("cityCode", "33010"); //33010 = 청주시
        urlBuilder.addEncodedQueryParameter ("nodeId", stationId);
        urlBuilder.addEncodedQueryParameter ("_type", "xml");

        Log.d(TAG,"DATA_GO_KR_SERVICE_KEY: "+ DATA_GO_KR_SERVICE_KEY ); //인증키 확인용
        Log.d(TAG,"메소드 fetchBusArrivals()의 정류소ID: "+ stationId );

        // Request 객체 생성
        Request request = new Request.Builder().url(urlBuilder.build()).build();

        Log.d("OkHttpRequest", request.toString());

        try {
            // OkHttp를 사용하여 동기 방식으로 요청 실행
            Response response = httpClient.newCall(request).execute();
            Log.d("OkHttpResponse", response.toString());

            //api 호출 성공시 if문 실행
            if (response.isSuccessful()) {
                InputStream is = response.body().byteStream();

                // XML 파싱 (이하 로직은 동일)
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(new InputStreamReader(is, "UTF-8"));

                String tag;
                String routeNo = "", arrTime = "", arrPrevCnt = "";
                StringBuilder arrivalResult = new StringBuilder();
                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        tag = xpp.getName();
                        if (tag.equals("routeno")) { xpp.next(); routeNo = xpp.getText(); }
                        else if (tag.equals("arrtime")) { xpp.next(); arrTime = xpp.getText(); }
                        else if (tag.equals("arrprevstationcnt")) { xpp.next(); arrPrevCnt = xpp.getText(); }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        tag = xpp.getName();
                        if (tag.equals("item") && !routeNo.isEmpty()) {
                            int arrivalSec = Integer.parseInt(arrTime);
                            String arrivalText = (arrivalSec / 60) + "분 후 도착";
                            arrivalResult.append("🚌 ").append(routeNo).append("번 (").append(arrPrevCnt).append(" 정거장 전)\n- ").append(arrivalText).append("\n\n");
                            routeNo = "";
                        }
                    }
                    eventType = xpp.next();
                }
                return arrivalResult.length() > 0 ? arrivalResult.toString().trim() : "도착 예정인 버스가 없습니다.";
            }
            else
            {
                // API 호출 실패 시
                Log.e(TAG, "fetchBusArrivals API Error: " + response.code() + " " + response.message());
                return "도착 정보 API 호출에 실패했습니다. (코드: " + response.code() + ")";
            }

        }
        catch (Exception e) {
            Log.e(TAG, "Error fetching bus arrivals: " + e.getClass().getName() + " - " + e.getMessage(), e);
            return "정보를 가져오는데 실패했습니다.";
        }
    }

}
