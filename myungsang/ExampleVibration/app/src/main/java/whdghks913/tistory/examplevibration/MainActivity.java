package whdghks913.tistory.examplevibration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	Vibrator vide;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void Vibrator_basic(View v){
		vide.vibrate(1000);
	}
	
	public void Vibrator_pattern(View v){

		long[] pattern = { 0, 500, 200, 400, 100 };

		vide.vibrate(pattern, -1);
	}

}
