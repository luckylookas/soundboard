import React, {ReactElement, ReactNode, useMemo} from "react";
import {Adventure} from "../api";

export const Box = ({children, title, onBack}: {children: ReactNode | undefined, title: string, onBack?: () => void}) => {
    return <div className={`flex flex-col w-full h-full rounded bg-emerald-50 shadow-lg shadow-neutral-500`}>
        <div className={'flex w-full flex-grow-0 bg-emerald-500 rounded-t p-2 justify-between items-center'}><h1>{title}</h1><div className={'rounded m-1 p-1 bg-rose-500 cursor-pointer select-none flex justify-center items-center'} onClick={onBack}>{`back`}</div></div>
        <div className={`flex flex-col flex-grow w-full justify-between `}>
        {children}
    </div>
    </div>
}

export const HtmlFileInput = ({id, onChange, inputProps, value}: HtmlInputProps<File, string>) => {
    return <div>
        <label htmlFor={id}
               className={`flex p-2     my-2 drop-shadow-lg rounded bg-emerald-300 hover:bg-emerald text-neutral-800 justify-center items-center`}> {value ? `change file (${value.name})` : 'select file'}</label>
        <input
            className={`hidden`}
            {...inputProps}
            type='file'
            name={id}
            id={id}
            onChange={e => {
                if (e.target.files?.length) {
                    onChange(e.target.files!.item(0)!)
                }
            }
            }/>
    </div>
}


interface HtmlInputProps<T, A> {
    onChange: (arg?: T) => void
    onSelect?: (arg?: A) => void
    id: string
    value: T | undefined
    additionalValues?: A[]
    inputProps: { [key: string]: string }
}

export const HtmlTextInput = ({id, onChange, inputProps, value, additionalValues, onSelect}: HtmlInputProps<string, Adventure>) => {
    return <div className={`relative w-full py-2 my-2`}>
        <input
            value={value}
            className={`w-full relative top-0 left-0 p-2 border-0 outline-0 border-b-2 border-neutral-500 rounded-t`}
            {...inputProps}
            type='text'
            name={id}
            id={id}
            onChange={e => onChange(e.target.value)}/>
        <label htmlFor={id} className={`absolute left-2   ${!!value ? 'text-xs top-0 text-neutral-200' : 'text-lg top-1 text-neutral-500'}`}>{id}</label>
        <div className={`shadow shadow-neutral-600 rounded-b`}>
            {additionalValues?.map((v, i) => <div
                onClick={() => onSelect ? onSelect(v) : () => {}}
                className={`p-2 ${i%2 ? 'bg-emerald-50': 'bg-emerald-100'} hover:bg-emerald-200 cursor-pointer`}>{v.name}</div>)}
        </div>
    </div>
}

interface ButtonProps {
    onClick: () => void
    disabled?: boolean
    accent?: boolean
    text: string
}


export const Button = ({text, onClick, disabled, accent}: ButtonProps) => {

    const color = useMemo(() => accent ? 'rose': 'emerald', [accent]);

    return <div aria-disabled={!!disabled} onClick={disabled ? () => {
    } : onClick} className={`
     cursor-${disabled ? 'not-allowed' : 'pointer'}
    select-none
    p-2
    justify-center
    items-center
    flex
    rounded 
    drop-shadow-lg
    bg-${color}-${disabled ? '950' : '300'} 
    hover:bg-${color}-${disabled ? '950' : '200'} 
    active:bg-${disabled ? '950' : '400'} 
    text-lg 
    flex-grow
    text-neutral-${disabled ? '500' : '800'}`}>
        {text}
    </div>
}

interface ToggleProps {
    children: ReactElement | string
    onClick: () => void
    checked: boolean
    slim?: boolean
}

export const Toggle = ({children, onClick, checked, slim}: ToggleProps) => {
    return <div onClick={onClick} className={`
    ${checked ? 'shadow-inner' : 'drop-shadow-lg'}
    cursor-pointer
    ${slim ? 'p-0': 'p-2'}
    select-none
    justify-center
    items-center
    flex
    flex-grow
    rounded 
    bg-emerald-${checked ? '600' : '300'} 
    hover:bg-emerald-200
    text-lg 
    text-neutral-800`}>
        {children}
    </div>
}


interface SliderProps {
    children: ReactElement | string
    onChange: (next: number) => void
    value: number,
}

export const Slider = ({children, onChange, value}: SliderProps) => {
    return <div className={`
    cursor-pointer
    p-2
    select-none
    drop-shadow-lg
    justify-center
    items-center
    flex`}>
        <label htmlFor={"volume"} className={`flex`}>{children}</label>
        <input className={`bg-neutral-200 w-full accent-emerald-600`} id={"volume"} name='volume' type={"range"} max={100} min={0} value={value}
               onChange={e => onChange(Number.parseInt(e.target.value))}/>
    </div>
}
