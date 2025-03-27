package com.example.tesisandroid;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MenuActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private MenuPagerAdapter menuPagerAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu2);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.BarraNavegacion);

        menuPagerAdapter = new MenuPagerAdapter(this);
        viewPager.setAdapter(menuPagerAdapter);

        // Conectar TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Home");
                        tab.setIcon(R.drawable.ic_home);
                        break;
                    case 1:
                        tab.setText("Subir");
                        tab.setIcon(R.drawable.ic_subir);
                        break;
                    case 2:
                        tab.setText("Buscar");
                        tab.setIcon(R.drawable.ic_buscar);
                        break;
                }
            }
        }).attach();
    }
}