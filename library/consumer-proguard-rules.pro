# keep getters/setters in RotatingDrawable so that animations can still work.
-keepclassmembers class com.getbase.floatingactionbutton.FloatingActionsMenu$RotatingDrawable {
   void set*(***);
   *** get*();
}
