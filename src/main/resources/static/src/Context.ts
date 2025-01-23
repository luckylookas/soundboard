import {createContext, Dispatch, SetStateAction} from "react";
import {Adventure, Scene, SoundDevice, SoundFile} from "./api";

export interface SoundboardContext {
    adventure?: Adventure
    devices?: SoundDevice[]
    selectedSceneId?: number
    refresh?: () => {}
    files?: SoundFile[]
}

export type Context = [SoundboardContext | undefined, Dispatch<SetStateAction<SoundboardContext | undefined>>]

export const AdventureContext = createContext<Context>([undefined, (a) => {}])
