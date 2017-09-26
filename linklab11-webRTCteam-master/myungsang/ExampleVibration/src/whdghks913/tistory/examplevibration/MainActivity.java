package whdghks913.tistory.examplevibration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
	// Vibrator�� �����մϴ�
	Vibrator vide;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// �ý��� ���񽺸� �ҷ��ɴϴ�
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
		 * ������ �︳�ϴ�
		 * ��ȣ ()�� ���ڴ� �и������� �ʷ� 1000�� 1���̴�
		 */
		vide.vibrate(1000);
		
		/**
		 * ���� �︮�� �ִ� ������ ������ �Ѵٸ�
		 * vide.cancel();
		 * �� ����ϸ� �˴ϴ�
		 */
	}
	
	public void Vibrator_pattern(View v){
		/**
		 * ù��° 0�� ���۽ð��Դϴ� ������ ������ �Ϸ��� 0�� �ƴ϶� �ٸ� ���� �����ʽÿ� 0�� ��� ���� �Դϴ�
		 * �ι�° ���� ������ �︱ �ð��Դϴ� 500�� 0.5��(1000=1��)�̹Ƿ� 0.5�ʰ� ������ �︳�ϴ�
		 * ����° ���� ������ �� �ð��Դϴ� 200�� 0.2���̹Ƿ� 0.2�ʰ� ������ �︮�� �ʽ��ϴ�
		 * �׹�° ���� �ι�° ���� �����ϴ�
		 * �ټ���° ���� ����° ���� �����ϴ�
		 * 
		 * �̷��� �Լ� ������ ����� �ֽ��ϴ�
		 * ���� ��� ������ ����(1��), ����(2��), ��(5��), ����(1��), ��(1��), ����(5��), ��(1��)......
		 * �� long[] pattern = { 1000, 2000, 5000, 1000, 1000, 5000, 1000 }; �Դϴ�
		 */
		
		//       index�� :   0    1     2      3     4
		long[] pattern = { 0, 500, 200, 400, 100 };
		/**
		 * index�� Ȯ�� ���
		 * Log.d("pattern index", "0:"+pattern[0]+" 1:"+pattern[1]+" 2:"+pattern[2]+" 3:"+pattern[3]+" 4:"+pattern[4]);
		 * 
		 * index���̶�? �迭���� n��° ���� �Դϴ�
		 */
		
		/**
		 * ù��°�� long�� ������, �ι�° ������ �ݺ� Ƚ���� �Է��մϴ�
		 * -1(����)�� 1�� �ݺ� �ϰڴٴ� ���Դϴ�
		 * 
		 * ���� ������ ����Ʈ ���� : the index into pattern at which to repeat, or -1 if you don't want to repeat.
		 * �� 0�� ����� ������ long[]���� index���� �����ϰڴٴ� ���� �˴ϴ�
		 * 
		 * ���� ��� vide.vibrate(pattern, 2); �� �ϰ� �Ǹ�
		 * 56��(long[])���� index 2������ "200"�Դϴ�
		 * �׷��Ƿ� 200, 400, 100�� ���� �ݺ��� �˴ϴ�
		 * 
		 * �� ���� ��� vide.vibrate(pattern, 3); �� �ϰ� �Ǹ�
		 * 56��(long[])���� index 3������ "400"�Դϴ�
		 * �׷��Ƿ� 400, 100�� ���� �ݺ��� �˴ϴ�
		 */
		vide.vibrate(pattern, -1);
	}

}
