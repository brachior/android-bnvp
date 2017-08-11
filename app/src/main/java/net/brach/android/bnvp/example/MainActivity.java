package net.brach.android.bnvp.example;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.brach.android.bnvp.BottomNavigationViewPager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int[] titles = new int[] {
                R.string.nav_home,
                R.string.nav_inbox,
                R.string.nav_camera,
                R.string.nav_settings
        };

        BottomNavigationViewPager pager = (BottomNavigationViewPager) findViewById(R.id.nav_pager);
        pager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return MyFragment.newInstance(getString(titles[position]));
            }

            @Override
            public int getCount() {
                return titles.length;
            }
        });
    }

    public static class MyFragment extends Fragment {
        final static String EXTRAS_TITLE = "title";

        public static Fragment newInstance(String title) {
            MyFragment fragment = new MyFragment();
            Bundle bundle = new Bundle();
            bundle.putString(EXTRAS_TITLE, title);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_layout, container, false);
            Bundle arguments = getArguments();
            ((TextView) view.findViewById(R.id.title)).setText(arguments.getString(EXTRAS_TITLE));
            return view;
        }
    }
}
