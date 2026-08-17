// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorNativeCall",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "CapacitorNativeCall",
            targets: ["NativeCallPlugin"]
        )
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "NativeCallPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NativeCallPlugin"
        )
    ]
)
