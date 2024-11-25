import {createContext, Dispatch, SetStateAction} from "react";
import {Adventure} from "./api";

export const AdventureContext = createContext<[Adventure | undefined, Dispatch<SetStateAction<Adventure | undefined>>]>([undefined, (a) => {}])
