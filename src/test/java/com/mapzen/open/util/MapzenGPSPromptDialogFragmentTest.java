package com.mapzen.open.util;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowAlertDialog;

import android.app.AlertDialog;
import android.content.Intent;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.getShadowApplication;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.util.FragmentTestUtil.startFragment;

@RunWith(MapzenTestRunner.class)
public class MapzenGPSPromptDialogFragmentTest {
    private MapzenGPSPromptDialogFragment dialogFragment;
    private AlertDialog dialog;
    private ShadowAlertDialog shadowDialog;

    @Before
    public void setUp() throws Exception {
        dialogFragment = new MapzenGPSPromptDialogFragment();
        startFragment(dialogFragment);
        dialogFragment.onCreateDialog(null);
        dialog = (AlertDialog) dialogFragment.getDialog();
        shadowDialog = shadowOf(dialog);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(dialogFragment).isNotNull();
    }

    @Test
    public void shouldDisplayGPSPromptTextCorrectly() throws Exception {
        assertThat(shadowDialog.getTitle())
                .isEqualTo(application.getString(R.string.gps_dialog_title));
        assertThat(shadowDialog.getMessage())
                .isEqualTo(application.getString(R.string.gps_dialog_message));
    }

    @Test
    public void onClickPositiveButton_shouldOpenGpsSettings() throws Exception {
        dialog.getButton(BUTTON_POSITIVE).performClick();
        Intent intent = getShadowApplication().getNextStartedActivity();
        assertThat(intent).isEqualTo(new Intent(ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Test
    public void onClickNegativeButton_shouldDismiss() throws Exception {
        dialog.getButton(BUTTON_NEGATIVE).performClick();
        assertThat(shadowOf(dialogFragment.getDialog()).hasBeenDismissed()).isTrue();
    }
}
