import os

filepath = r"d:\Projects\zoron\src\WhyredController\app\src\main\res\layout\activity_main.xml"

with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

# Replace the root CoordinatorLayout with a FrameLayout wrapper
if "<androidx.coordinatorlayout.widget.CoordinatorLayout" in content:
    # Find the end of the root tag's attributes
    root_idx = content.find("<androidx.coordinatorlayout.widget.CoordinatorLayout")
    # We want to wrap the whole thing.
    
    # We'll just replace the first tag and the last tag.
    new_content = content.replace(
        """<androidx.coordinatorlayout.widget.CoordinatorLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_gradient_main"
    android:fitsSystemWindows="true">""",
        """<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black">
    
    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/composeRoot"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <androidx.coordinatorlayout.widget.CoordinatorLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:visibility="gone">"""
    )
    
    new_content = new_content.replace("</androidx.coordinatorlayout.widget.CoordinatorLayout>", "</androidx.coordinatorlayout.widget.CoordinatorLayout>\n</FrameLayout>")
    
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(new_content)
        
    print("Wrapped activity_main.xml successfully.")
else:
    print("CoordinatorLayout not found.")
