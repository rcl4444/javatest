package cn.zhijian.passport.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Option<T> {

	final T value;

	final String label;
}
