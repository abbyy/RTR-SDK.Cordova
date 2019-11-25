// ABBYY® Mobile Capture © 2019 ABBYY Production LLC.
// ABBYY is a registered trademark or a trademark of ABBYY Software Ltd.

package com.abbyy.mobile.rtr.cordova.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.abbyy.mobile.rtr.Language;
import com.abbyy.mobile.rtr.cordova.ResourcesUtils;
import com.abbyy.mobile.rtr.cordova.RtrManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( "deprecation" )
public class LanguagesSettingActivity extends PreferenceActivity {

	private List<CheckBoxPreference> checkBoxes;

	@Override
	protected void onCreate( @Nullable Bundle savedInstanceState )
	{
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
			WindowManager.LayoutParams.FLAG_FULLSCREEN );
		super.onCreate( savedInstanceState );
		setContentView( ResourcesUtils.getResId( "layout", "languages_setting_activity", this ) );

		PreferenceScreen screen = getPreferenceManager().createPreferenceScreen( this );

		checkBoxes = new ArrayList<>();
		for( Language language : RtrManager.getLanguages() ) {
			CheckBoxPreference checkBoxPreference = new CheckBoxPreference( this );
			checkBoxPreference.setTitle( language.name() );
			checkBoxPreference.setKey( language.name() );
			checkBoxes.add( checkBoxPreference );
			screen.addPreference( checkBoxPreference );
		}

		setPreferenceScreen( screen );

		TextView okButton = findViewById( ResourcesUtils.getResId( "id", "ok_button", this ) );
		okButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v )
			{
				closeActivity();
			}
		} );
	}

	public static Intent newIntent( Context context )
	{
		return new Intent( context, LanguagesSettingActivity.class );
	}

	@Override
	public void onBackPressed()
	{
		closeActivity();
	}

	private void closeActivity()
	{
		boolean isChecked = false;
		for( CheckBoxPreference checkBox : checkBoxes ) {
			if( checkBox.isChecked() ) {
				isChecked = true;
				break;
			}
		}
		if( isChecked ) {
			this.finish();
			overridePendingTransition( 0, ResourcesUtils.getResId( "anim", "from_right_to_left", this ) );
		} else {
			Toast.makeText( this, "Check at least one language", Toast.LENGTH_SHORT ).show();
		}
	}
}
