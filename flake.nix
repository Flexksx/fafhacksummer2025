{
  description = "Reproducible Flutter dev environment + Android emulator";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config = {
            allowUnfree = true; # Android Studio, fonts …
            android_sdk.accept_license = true; # Google + Android licences
          };
        };

        # One self-contained SDK that already contains the emulator +
        # all the system-images we need.
        androidComposition = pkgs.androidenv.composeAndroidPackages {
          # -- SDK command-line tools + emulator core -------------------
          cmdLineToolsVersion = "11.0";
          includeEmulator = true;
          emulatorVersion = "34.1.9";

          # -- you only need the *latest* platform + build-tools ----------
          platformVersions = [ "34" ];
          buildToolsVersions = [ "34.0.0" ];

          # -- one ABI that matches your host ---------------------------
          abiVersions = [ "x86_64" ];
          systemImageTypes = [ "google_apis_playstore" ];
          includeSystemImages = true;

          # -- Comment this out unless you compile native/FFI code ------
          # includeNDK           = true;
          # ndkVersions          = [ "26.3.11579264" ];

          # No CMake unless you actually open a C++ sub-project
          # cmakeVersions        = [ "3.22.1" ];
        };

        androidSdk = androidComposition.androidsdk;
      in
      {
        devShells.default =
          # local variable visible only inside this mkShell
          let
            androidHome = "${androidSdk}/libexec/android-sdk";
          in
          pkgs.mkShell {
            name = "flutter-dev-shell";

            buildInputs = with pkgs; [
              flutter
              androidSdk
              jdk17
              qemu_kvm
            ];

            # export environment variables
            ANDROID_HOME = androidHome;
            ANDROID_SDK_ROOT = androidHome;
            JAVA_HOME = pkgs.jdk17.home;

            GRADLE_OPTS = ''
              -Dorg.gradle.project.android.aapt2FromMavenOverride=${androidHome}/build-tools/34.0.0/aapt2
            '';

            shellHook = ''
              echo "✓ Flutter & Android SDK initialised."
            '';
          };

      }
    );
}
