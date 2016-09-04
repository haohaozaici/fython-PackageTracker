package info.papdt.express.helper.ui;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ClipboardUtils;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.support.Settings;
import info.papdt.express.helper.ui.adapter.DetailsInfoAdapter;
import info.papdt.express.helper.ui.common.AbsActivity;

public class DetailsActivity extends AbsActivity {

	private CollapsingToolbarLayout mToolbarLayout;
	private RecyclerView mRecyclerView;
	private FloatingActionButton mFAB;
	private ImageView mBackground;
	private AppCompatEditText mNameEdit;

	private DetailsInfoAdapter mAdapter;

	private AlertDialog mEditDialog, mDeleteDialog;

	private Package data;
	private int state;

	private static final String EXTRA_PACKAGE_JSON = "extra_package_json", EXTRA_STATE = "extra_state";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		super.onCreate(savedInstanceState);

		state = getIntent().getIntExtra(EXTRA_STATE, Package.STATUS_FAILED);

		setContentView(R.layout.activity_details);

		mActionBar.setDisplayHomeAsUpEnabled(true);

		setUpData();
	}

	@Override
	protected void setUpViews() {
		mFAB = $(R.id.fab);
		mBackground = $(R.id.parallax_background);
		mRecyclerView = $(R.id.recycler_view);
		mNameEdit = new AppCompatEditText(this);
		mToolbarLayout = $(R.id.collapsing_layout);

		mNameEdit.setSingleLine(true);

		mRecyclerView.setHasFixedSize(false);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

		mFAB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showNameEditDialog();
			}
		});
	}

	private void setUpData() {
		mAdapter = new DetailsInfoAdapter(this);
		mRecyclerView.setAdapter(mAdapter);
		new ListBuildTask().execute();

		Drawable drawable = mFAB.getDrawable();
		if (mFAB.getDrawable() == null) {
			drawable = DrawableCompat.wrap(getResources().getDrawable(R.drawable.ic_create_black_24dp));
			DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
			mFAB.setImageDrawable(drawable);
		}

		int color, colorDark;
		if (state == Package.STATUS_DELIVERED) {
			mBackground.setImageResource(R.drawable.banner_background_delivered);
			color = getResources().getColor(R.color.teal_500);
			colorDark = getResources().getColor(R.color.teal_700);
		} else if (state == Package.STATUS_FAILED) {
			mBackground.setImageResource(R.drawable.banner_background_error);
			color = getResources().getColor(R.color.blue_grey_500);
			colorDark = getResources().getColor(R.color.blue_grey_700);
		} else {
			mBackground.setImageResource(R.drawable.banner_background_on_the_way);
			color = getResources().getColor(R.color.blue_500);
			colorDark = getResources().getColor(R.color.blue_700);
		}
		mToolbarLayout.setContentScrimColor(color);
		mToolbarLayout.setStatusBarScrimColor(colorDark);
		DrawableCompat.setTint(drawable, color);
		if (getSettings().getBoolean(Settings.KEY_NAVIGATION_TINT, true) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setNavigationBarColor(colorDark);
		}
	}

	private ArrayList<DetailsInfoAdapter.ItemType> buildItems() {
		ArrayList<DetailsInfoAdapter.ItemType> items = new ArrayList<>();

		items.add(new DetailsInfoAdapter.ItemType(DetailsInfoAdapter.ItemType.TYPE_NORMAL, DetailsInfoAdapter.ItemType.ID_NAME));
		items.add(new DetailsInfoAdapter.ItemType(DetailsInfoAdapter.ItemType.TYPE_NORMAL, DetailsInfoAdapter.ItemType.ID_NUMBER));
		items.add(new DetailsInfoAdapter.ItemType(DetailsInfoAdapter.ItemType.TYPE_SUBHEADER, DetailsInfoAdapter.ItemType.ID_STATUS_HEADER));

		if(data.data != null) {
			for (int i = 0; i < data.data.size(); i++) {
				DetailsInfoAdapter.ItemType item = new DetailsInfoAdapter.ItemType(DetailsInfoAdapter.ItemType.TYPE_PACK_STATUS, DetailsInfoAdapter.ItemType.ID_STATUS);
				item.statusIndex = i;
				items.add(item);
			}
		}

		return items;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_details, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_copy_code) {
			ClipboardUtils.putString(getApplicationContext(), data.number);
			Snackbar.make($(R.id.coordinator_layout), R.string.toast_copied_code, Snackbar.LENGTH_LONG)
					.show();
			return true;
		} else if (id == R.id.action_share) {
			showShareChooser();
			return true;
		} else if (id == R.id.action_delete) {
			showDeleteDialog();
			return true;
		} else if (id == R.id.action_set_unread) {
			data.unreadNew = true;

			Intent intent = new Intent();
			intent.putExtra("id", data.number);
			setResult(MainActivity.RESULT_RENAMED, intent);

			PackageDatabase db = PackageDatabase.getInstance(getApplicationContext());
			db.set(db.indexOf(data.number), data);

			finish();
			return true;
		}else if (id == R.id.map_details) {
			String pack = getIntent().getStringExtra(EXTRA_PACKAGE_JSON);
			Intent intent = new Intent(this, MapActivity.class);
			intent.putExtra("Package", pack);
			startActivity(intent);

		}
		return super.onOptionsItemSelected(item);
	}

	private void showNameEditDialog() {
		if (mEditDialog == null) {
			int DP8 = (int) ScreenUtils.dpToPx(this, 8);
			mEditDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_edit_name_title)
					.setView(mNameEdit, DP8, DP8, DP8, DP8)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							if (!TextUtils.isEmpty(mNameEdit.getText().toString())) {
								data.name = mNameEdit.getText().toString().trim();
								mAdapter.notifyDataSetChanged();

								Intent intent = new Intent();
								intent.putExtra("id", data.number);
								setResult(MainActivity.RESULT_RENAMED, intent);

								new Thread() {
									@Override
									public void run() {
										PackageDatabase db = PackageDatabase.getInstance(getApplicationContext());
										db.set(db.indexOf(data.number), data);
										db.save();
									}
								}.start();
							} else {
								Snackbar.make($(R.id.coordinator_layout), R.string.toast_edit_name_is_empty, Snackbar.LENGTH_SHORT)
										.show();
							}
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

						}
					})
					.create();
		}
		mNameEdit.setText(data.name);
		mNameEdit.setSelection(data.name.length());
		mEditDialog.show();
	}

	private void showDeleteDialog() {
		if (mDeleteDialog == null) {
			mDeleteDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_delete_title)
					.setMessage(R.string.dialog_delete_message)
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							PackageDatabase.getInstance(getApplicationContext()).remove(data);

							Intent intent = new Intent();
							intent.putExtra("title", data.name);
							setResult(MainActivity.RESULT_DELETED, intent);
							finish();
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {

						}
					})
					.create();
		}
		mDeleteDialog.show();
	}

	private void showShareChooser() {
		String text = getString(R.string.share_info_format,
				data.name,
				data.number,
				data.companyChineseName,
				data.data.size() > 0 ? data.data.get(0).context : "Unknown",
				data.data.size() > 0 ? data.data.get(0).time : ""
		);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(intent, getString(R.string.dialog_share_title)));
	}

	public static void launch(AppCompatActivity activity, Package p) {
		Intent intent = new Intent(activity, DetailsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(EXTRA_PACKAGE_JSON, p.toJsonString());
		intent.putExtra(EXTRA_STATE, p.getState());
		activity.startActivityForResult(intent, MainActivity.REQUEST_DETAILS);
	}

	private class ListBuildTask extends AsyncTask<Void, Void, ArrayList<DetailsInfoAdapter.ItemType>> {

		@Override
		protected ArrayList<DetailsInfoAdapter.ItemType> doInBackground(Void... voids) {
			data = Package.buildFromJson(getIntent().getStringExtra(EXTRA_PACKAGE_JSON));

			PackageDatabase db = PackageDatabase.getInstance(getApplicationContext());
			if (data.unreadNew) {
				data.unreadNew = false;

				Intent intent = new Intent();
				intent.putExtra("id", data.number);
				setResult(MainActivity.RESULT_RENAMED, intent);
			}
			db.set(db.indexOf(data.number), data);
			db.save();

			return buildItems();
		}

		@Override
		protected void onPostExecute(ArrayList<DetailsInfoAdapter.ItemType> items) {
			mAdapter.setData(data, items);
			mAdapter.notifyDataSetChanged();

			int color;
			if (state == Package.STATUS_DELIVERED) {
				color = getResources().getColor(R.color.teal_500);
			} else if (state == Package.STATUS_FAILED) {
				color = getResources().getColor(R.color.blue_grey_500);
			} else {
				color = getResources().getColor(R.color.blue_500);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(data.name, null, color);
				setTaskDescription(taskDesc);
			}
		}

	}

}
