package com.getbase.floatingactionbutton.sample;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import android.app.Activity;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

	  ImageButton button = (ImageButton)findViewById(R.id.image_button);
	  button.setAlpha(0.3f);

 		ImageButton button2 = (ImageButton)findViewById(R.id.image_button2);
	  button2.setAlpha(0.3f);


	  FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.action_d);
	  fab.getBackground().setAlpha(30);

  }
}
