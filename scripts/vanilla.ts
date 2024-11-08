// @ts-ignore
import {Array, Partial, Record, Tuple, String, Dictionary, Boolean, Number, Union, Literal, Static, Runtype} from "https://raw.githubusercontent.com/alex5nader/runtypes/master/src/index.ts";

export type Vec3 = Static<typeof Vec3>;
export const Vec3 = Tuple(Number, Number, Number);

export type GuiLight = Static<typeof GuiLight>;
export const GuiLight = Union(Literal("front"), Literal("side"));

export type ItemTransform = Static<typeof ItemTransform>;
export const ItemTransform = Partial({
    rotation: Vec3,
    translation: Vec3,
    scale: Vec3,
});

export type ItemTransforms = Static<typeof ItemTransforms>;
export const ItemTransforms = Partial({
    thirdperson_righthand: ItemTransform,
    thirdperson_lefthand: ItemTransform,
    firstperson_righthand: ItemTransform,
    firstperson_lefthand: ItemTransform,
    gui: ItemTransform,
    head: ItemTransform,
    ground: ItemTransform,
    fixed: ItemTransform,
});

export type TextureMap = Static<typeof TextureMap>;
export const TextureMap = Partial({
    particle: String,
}).And(Dictionary(String));

export type Rotation = Static<typeof Rotation>;
export const Rotation = Record({
    origin: Vec3,
    axis: Union(Literal("x"), Literal("y"), Literal("z")),
    angle: Union(Literal(-45), Literal(-22.5), Literal(0), Literal(22.5), Literal(45)),
}).And(Partial({
    rescale: Boolean
}));

export type Direction = Static<typeof Direction>;
export const Direction = Union(Literal("down"), Literal("up"), Literal("north"), Literal("south"), Literal("west"), Literal("east"));

export type Face = Static<typeof Face>;
export const Face = Record({
    texture: String,
}).And(Partial({
    uv: Tuple(Number, Number, Number, Number),
    cullface: Direction,
    rotation: Union(Literal(0), Literal(90), Literal(180), Literal(270)),
    tintindex: Number,
}));

export function FacesOf<A extends Runtype>(Face: A) {
    return Partial({
        down: Face,
        up: Face,
        north: Face,
        south: Face,
        west: Face,
        east: Face,
    });
}

export type Faces = Static<typeof Faces>;
export const Faces = FacesOf(Face);

export function ModelElementOf<A extends Runtype>(Faces: A) {
    return Record({
        from: Vec3,
        to: Vec3,
        faces: Faces
    }).And(Partial({
        rotation: Rotation,
        shade: Boolean,
        lightEmission: Number
    }));
}

export type ModelElement = Static<typeof ModelElement>;
export const ModelElement = ModelElementOf(Faces);

export function ModelOf<A extends Runtype>(ModelElement: A) {
    return Partial({
        parent: String,
        elements: Array(ModelElement),
        textures: TextureMap,
        ambientocclusion: Boolean,
        guiLight: GuiLight,
        itemTransforms: ItemTransforms,
        elements: Array(ModelElement),
    });
}

export type Model = Static<typeof Model>;
export const Model = ModelOf(ModelElement);
