import React, {ReactElement, ReactNode} from "react";

export const Box = ({children, title}: {children: ReactNode | undefined, title: string}) => {
    return <div className={`flex flex-col w-full h-full rounded bg-emerald-50 shadow-lg shadow-neutral-500`}>
        <div className={'flex w-full flex-grow-0 bg-emerald-500 p-2 rounded-t'}><h1>{title}</h1></div>
        <div className={`flex flex-col flex-grow w-full justify-between p-2 `}>
        {children}
    </div>
    </div>
}


interface HtmlInputProps<T> {
    onAction: (arg?: T) => void
    id: string
    value: T | undefined
    inputProps: { [key: string]: string }
}

export const HtmlFileInput = ({id, onAction, inputProps, value}: HtmlInputProps<File>) => {
    return <div>
        <label htmlFor={id}
               className={`flex p-2 drop-shadow-lg rounded bg-emerald-300 hover:bg-emerald-200 text-neutral-800 justify-center items-center`}> {value ? `change file (${value.name})` : 'select file'}</label>
        <input
            className={`hidden`}
            {...inputProps}
            type='file'
            name={id}
            id={id}
            onChange={e => {
                if (e.target.files?.length) {
                    onAction(e.target.files!.item(0)!)
                }
            }
            }/>
    </div>
}


interface ButtonProps {
    onClick: () => void
    disabled?: boolean
    text: string
}


export const Button = ({text, onClick, disabled}: ButtonProps) => {
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
    bg-emerald-${disabled ? '950' : '300'} 
    hover:bg-emerald-${disabled ? '950' : '200'} 
    active:bg-${disabled ? '950' : '400'} 
    text-lg 
    text-neutral-${disabled ? '500' : '800'}`}>
        {text}
    </div>
}

interface ToggleProps {
    children: ReactElement | string
    onClick: () => void
    checked: boolean
}

export const Toggle = ({children, onClick, checked}: ToggleProps) => {
    return <div onClick={onClick} className={`
    ${checked ? 'shadow-inner' : 'drop-shadow-lg'}
    cursor-pointer
    p-2
    select-none
    justify-center
    items-center
    flex
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
