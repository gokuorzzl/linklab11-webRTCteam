package whdghks913.tistory.examplevibration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	// Vibrator를 정의합니다
	Vibrator vide;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// 시스탬 서비스를 불러옵니다
		vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void Vibrator_basic(View v){
		/**
		 * 진동을 울립니다
		 * 괄호 ()안 숫자는 밀리세컨드 초로 1000이 1초이다
		 */
		vide.vibrate(1000);
		
		/**
		 * 만약 울리고 있는 진동을 끌려고 한다면
		 * vide.cancel();
		 * 을 사용하면 됩니다
		 */
	}
	
	public void Vibrator_pattern(View v){
		/**
		 * 첫번째 0은 시작시간입니다 지연된 시작을 하려면 0이 아니라 다른 값을 넣으십시오 0은 즉시 시작 입니다
		 * 두번째 값은 진동이 울릴 시간입니다 500은 0.5초(1000=1초)이므로 0.5초간 진동이 울립니다
		 * 세번째 값은 진동을 쉴 시간입니다 200은 0.2초이므로 0.2초간 진동이 울리지 않습니다
		 * 네번째 값은 두번째 값과 같습니다
		 * 다섯번째 값은 세번째 값과 같습니다
		 * 
		 * 이렇게 게속 패턴을 만들수 있습니다
		 * 예를 들어 지연된 시작(1초), 진동(2초), 쉼(5초), 진동(1초), 쉼(1초), 진동(5초), 쉼(1초)......
		 * 는 long[] pattern = { 1000, 2000, 5000, 1000, 1000, 5000, 1000 }; 입니다
		 */
		
		//       index값 :   0    1     2      3     4
		long[] pattern = { 0, 500, 200, 400, 100 };
		/**
		 * index값 확인 방법
		 * Log.d("pattern index", "0:"+pattern[0]+" 1:"+pattern[1]+" 2:"+pattern[2]+" 3:"+pattern[3]+" 4:"+pattern[4]);
		 * 
		 * index값이란? 배열에서 n번째 값을 입니다
		 */
		
		/**
		 * 첫번째는 long형 패턴을, 두번째 값에는 반복 횟수를 입력합니다
		 * -1(음수)은 1번 반복 하겠다는 뜻입니다
		 * 
		 * 구글 개발자 사이트 원문 : the index into pattern at which to repeat, or -1 if you don't want to repeat.
		 * 즉 0과 양수를 넣으면 long[]에서 index부터 시작하겠다는 뜻이 됩니다
		 * 
		 * 예를 들면 vide.vibrate(pattern, 2); 로 하게 되면
		 * 56줄(long[])에서 index 2번값은 "200"입니다
		 * 그러므로 200, 400, 100만 무한 반복이 됩니다
		 * 
		 * 또 예를 들면 vide.vibrate(pattern, 3); 로 하게 되면
		 * 56줄(long[])에서 index 3번값은 "400"입니다
		 * 그러므로 400, 100만 무한 반복이 됩니다
		 */
		vide.vibrate(pattern, -1);
	}

}
