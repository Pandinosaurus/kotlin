@_implementationOnly import KotlinBridges_main
import KotlinRuntime
import KotlinRuntimeSupport
import KotlinStdlib

public final class Enum: KotlinRuntime.KotlinBase, KotlinRuntimeSupport._KotlinBridged, Swift.CaseIterable {
    public static var a: main.Enum {
        get {
            return main.Enum.__createClassWrapper(externalRCRef: Enum_a_get())
        }
    }
    public static var allCases: [main.Enum] {
        get {
            return Enum_entries_get() as! Swift.Array<main.Enum>
        }
    }
    public static var b: main.Enum {
        get {
            return main.Enum.__createClassWrapper(externalRCRef: Enum_b_get())
        }
    }
    public var i: Swift.Int32 {
        get {
            return Enum_i_get(self.__externalRCRef())
        }
        set {
            return Enum_i_set__TypesOfArguments__Swift_Int32__(self.__externalRCRef(), newValue)
        }
    }
    package override init(
        __externalRCRefUnsafe: Swift.UnsafeMutableRawPointer?,
        options: KotlinRuntime.KotlinBaseConstructionOptions
    ) {
        super.init(__externalRCRefUnsafe: __externalRCRefUnsafe, options: options)
    }
    public func print() -> Swift.String {
        return Enum_print(self.__externalRCRef())
    }
    public static func valueOf(
        value: Swift.String
    ) -> main.Enum {
        return main.Enum.__createClassWrapper(externalRCRef: Enum_valueOf__TypesOfArguments__Swift_String__(value))
    }
}
public func enumId(
    e: ExportedKotlinPackages.kotlin.Enum
) -> ExportedKotlinPackages.kotlin.Enum {
    return ExportedKotlinPackages.kotlin.Enum.__createClassWrapper(externalRCRef: __root___enumId__TypesOfArguments__ExportedKotlinPackages_kotlin_Enum__(e.__externalRCRef()))
}
