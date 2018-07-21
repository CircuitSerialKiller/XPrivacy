package biz.bokhorst.xprivacy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/*My additions*/
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;


public class BootReceiver extends BroadcastReceiver {
/*
	int xposedVersion = 0;
//http://www.java2s.com/Open-Source/Android_Free_Code/UI/app/hk_valenta_completeactionplusMainPagerActivity_java.htm
	private boolean existXposed() {
		try {
			if (getActivity().getPackageManager().getPackageInfo("de.robv.android.xposed.installer", PackageManager.GET_META_DATA) != null) {
				xposedVersion = 1;
				return true;
			}
		} catch (NameNotFoundException e1) {
			// not found package
		}
		try {
			if (getPackageManager().getPackageInfo("pro.burgerz.wsm.manager", PackageManager.GET_META_DATA) != null) {
				xposedVersion = 2;
				return true;
			}
		} catch (NameNotFoundException e1) {
			// not found package
		}

		return false;
	}
*/
	@Override
	public void onReceive(final Context context, Intent bootIntent) {
		// Start boot update
		Intent changeIntent = new Intent();
		changeIntent.setClass(context, UpdateService.class);
		changeIntent.putExtra(UpdateService.cAction, UpdateService.cActionBoot);
		context.startService(changeIntent);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// Check if Xposed enabled
		if (Util.isXposedEnabled() && PrivacyService.checkClient())
			try {
				if (PrivacyService.getClient().databaseCorrupt()) {
					// Build notification
					NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
					notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
					notificationBuilder.setContentTitle(context.getString(R.string.app_name));
					notificationBuilder.setContentText(context.getString(R.string.msg_corrupt));
					notificationBuilder.setWhen(System.currentTimeMillis());
					notificationBuilder.setAutoCancel(true);
					Notification notification = notificationBuilder.build();

					// Display notification
					notificationManager.notify(Util.NOTIFY_CORRUPT, notification);
				} else
					context.sendBroadcast(new Intent("biz.bokhorst.xprivacy.action.ACTIVE"));
			} catch (Throwable ex) {
				Util.bug(null, ex);
			}
		else {
			// Create Xposed installer intent
			// @formatter:off
			Intent xInstallerIntent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION")
				.setPackage("de.robv.android.xposed.installer")
				.putExtra("section", "modules")
				.putExtra("module", context.getPackageName())
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// @formatter:on

			PendingIntent pi = (xInstallerIntent == null ? null : PendingIntent.getActivity(context, 0,
					xInstallerIntent, PendingIntent.FLAG_UPDATE_CURRENT));

			// Build notification
			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
			notificationBuilder.setSmallIcon(R.drawable.ic_launcher);
			notificationBuilder.setContentTitle(context.getString(R.string.app_name));
			notificationBuilder.setContentText(context.getString(R.string.app_notenabled));
			notificationBuilder.setWhen(System.currentTimeMillis());
			notificationBuilder.setAutoCancel(true);
			if (pi != null)
				notificationBuilder.setContentIntent(pi);
			Notification notification = notificationBuilder.build();

			// Display notification
			notificationManager.notify(Util.NOTIFY_NOTXPOSED, notification);
		}
	}
}
