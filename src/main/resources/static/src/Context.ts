import {createContext, Dispatch, SetStateAction} from "react";
import {Adventure} from "./api";

export type Context = [Adventure | undefined, Dispatch<SetStateAction<Adventure | undefined>>]
export const AdventureContext = createContext<Context>([undefined, (a) => {}])
