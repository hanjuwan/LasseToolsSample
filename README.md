LasseTools는 앱개발에 필요한 여러가지 유틸기능과 디버깅을 지원하는 라이브러리 입니다.
웹뷰 크롬디버깅 및 자바리플렉션을 활용하여 스크립트를 이용하여 
여러가지 디버깅 기능을 수행하고 스크립트를 통해 자바 메서드에 접근가능한 기능을 제공합니다

LasseTools


- 개발전용 토스트

  로그보다 토스트를 즐겨서 사용하는 분이 많습니다. 하지만 대부분의 폰에서는
  
  토스트를 여러번찍으면 스택에 쌓여서 밀리는 현상 및 씹히는 현상을 많이 볼수 있습니다.
  
  DToast를 사용하면 2초이내에 오는 토스트를 취합하여 차례대로 보여줍니다. 
  
  ex) 1, 2, 3 으로 메시지를 연속적으로 찍을 시 아래와 같이 같이 표기됩니다.
  
  1
  
  2
  
  3 
  
  
  LasseTools.DToast(Activity activity, String string);

- 개발자모드 가져오기
  
  LasseTools.getInstance().isDevModeEnabled()

- USB디버깅 옵션 가져오기
  
  LasseTools.getInstance().isUsbDebuggingEnabled()
  
- 크롬디버깅 연결을 통한 기능(캡쳐, 로그수집, 메서드접근(액티비티/프래그먼트))

  초기설정 - Application Class 혹은 초기 Activity 에서 
  
  LasseTools.getInstance().init(this, BuildConfig.DEBUG);
  


LassePermission
- 런타임 퍼미션 받기

  String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
  
  LassePermission.getPermission(this, permissions, new LassePermission.PermissionListener() {
  
      @Override
      
      public void onRequestResult(boolean allGranted, ArrayList<String> deniedPermissions) {
      
          Toast.makeText(MainActivity.this, "getPermission Result : " + allGranted, Toast.LENGTH_SHORT).show();
          
      }
      
  });
  
- 오버레이 퍼미션받기

  LassePermission.getOverlay(this, new LassePermission.OverlayListener() {
  
      @Override
      
      public void onCheckCompleted(boolean result) {
      
          Toast.makeText(MainActivity.this, "getOverlay Result : " + result, Toast.LENGTH_SHORT).show();
          
      }
      
  });
  
- 사용자통계권한 받기(추가예정)
